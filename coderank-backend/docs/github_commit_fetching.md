# GitHub Commit Count Fetching Architecture

This document explains how the CodeRank backend retrieves the total number of public commits for any given GitHub user profile without requiring user authentication (Personal Access Tokens).

---

## 1. The Challenge
GitHub's standard user profile API (`GET https://api.github.com/users/{username}`) provides basic stats such as public repositories, followers, and following. However, **it does not expose the total number of commits** made by the user.

Typically, developers fetch commit stats via:
1. **GitHub GraphQL API**: Requires a Personal Access Token (PAT) for every request, which complicates a public-facing developer evaluation dashboard.
2. **HTML Scraping**: Parsing the user's contribution grid. This is fragile and breaks whenever GitHub updates its frontend layout.

---

## 2. The Solution: GitHub Commit Search API
To fetch public commits cleanly without authentication, the backend utilizes GitHub’s Search Commits API. By scoping the search to an author, we get an accurate total count of all commits indexed by GitHub.

### Endpoint Specifications
* **URL**: `https://api.github.com/search/commits`
* **HTTP Method**: `GET`
* **Query Parameters**:
  * `q=author:{username}` — Filters search results to only return commits authored by the specified user.
* **Required Headers**:
  * `Accept: application/vnd.github.cloak-preview` — Enables the commit search preview features (required by GitHub's API routing).

### Example Raw API Request
```bash
curl -H "Accept: application/vnd.github.cloak-preview" \
     "https://api.github.com/search/commits?q=author:manojkumar-ctrl"
```

### Example JSON Response Schema
```json
{
  "total_count": 132,
  "incomplete_results": false,
  "items": [
    {
      "sha": "522ac3efc28bf0c33a8f53fe395fe747ac91d7dd",
      "commit": {
        "message": "temp deploy"
      }
    }
  ]
}
```
* **Key Metric**: The `"total_count"` field contains the exact aggregate number of public commits made by the user.

---

## 3. Implementation Workflow

### Step 1: Input Cleansing
When the frontend submits a full URL (e.g. `https://github.com/manojkumar-ctrl`), the backend extracts the final path segment to isolate the username:
```java
// Logic inside ProfileService.java
private String extractUsername(String input) {
    if (input.startsWith("http")) {
        if (input.endsWith("/")) input = input.substring(0, input.length() - 1);
        String[] parts = input.split("/");
        return parts[parts.length - 1];
    }
    return input;
}
```

### Step 2: Fetching via WebClient
`GithubClient.java` calls the Search API asynchronously using `WebClient` and extracts the `total_count`:
```java
public Integer fetchTotalCommits(String username) {
    try {
        Map response = webClient.get()
                .uri("/search/commits?q=author:{username}", username)
                .header("Accept", "application/vnd.github.cloak-preview")
                .retrieve()
                .bodyToMono(Map.class)
                .block();
        
        if (response != null && response.containsKey("total_count")) {
            Object count = response.get("total_count");
            if (count instanceof Number) {
                return ((Number) count).intValue();
            }
        }
        return 0;
    } catch (Exception e) {
        return 0; // Fallback to 0 if rate limited or server down
    }
}
```

### Step 3: Database Storage
The `ProfileService` calls this client, captures the integer value, and saves it into the `github_stats` cache table:
```java
GithubStats stats = githubStatsRepository.findByUser(user).orElse(new GithubStats());
stats.setTotalCommits(githubClient.fetchTotalCommits(username));
githubStatsRepository.save(stats);
```

---

## 4. Limitations & Considerations
* **Rate Limiting**: Without a token, GitHub limits search requests to **30 requests per minute** per IP address. Since CodeRank uses this as a cached sync step (only triggered during a manual "Analyze Profile" event), this limit is highly manageable for general development.
* **Scope**: This method only counts commits made in **public repositories** that have been indexed by GitHub. Private repository commits will not be counted unless an authenticated session token is supplied.

---

## 5. API Optimization & Scaling Techniques

To scale this feature in a high-traffic production environment, several optimization patterns can be applied:

### A. Memory Caching with Redis
* **The Strategy**: Querying external APIs or the database on every dashboard load is expensive. We can cache the final compiled profile stats (JSON) in Redis with a TTL of 12 to 24 hours.
* **Workflow**:
  ```
  Dashboard Request ──> Redis Check (Cache Hit?) ──[Yes]──> Return Cached JSON
                             │
                            [No]
                             ▼
                    Fetch Database/APIs ──> Update Redis Cache ──> Return
  ```

### B. Parallel API Fetching (Asynchronous Processing)
* **The Strategy**: Right now, the backend fetches GitHub, LeetCode, and Codeforces sequentially. If each API takes 1 second, the user waits 3+ seconds.
* **Optimization**: Use Java's `CompletableFuture` or Spring's `@Async` to fire all three external requests in parallel:
  ```java
  CompletableFuture<Map> githubFuture = CompletableFuture.supplyAsync(() -> githubClient.fetchProfile(user));
  CompletableFuture<Map> leetcodeFuture = CompletableFuture.supplyAsync(() -> leetcodeClient.fetchStats(user));
  
  // Wait for all to complete concurrently
  CompletableFuture.allOf(githubFuture, leetcodeFuture).join();
  ```
  This caps the total wait time to the duration of the single slowest API call (usually ~1 second).

### C. Rate Limit Management (Personal Access Tokens)
* **The Strategy**: Inject a system-wide GitHub Personal Access Token (PAT) via the `.env` configuration file.
* **Result**: Authenticated requests immediately boost the GitHub API rate limit from **30 requests/min** to **5,000 requests/hour**, preventing production outages under heavy user loads.

### D. Resilience & Circuit Breakers (Resilience4j)
* **The Strategy**: If LeetCode or GitHub goes down, we do not want our backend threads hanging or throwing 500 errors to the user.
* **Optimization**: Implement a **Circuit Breaker** pattern. If external API calls begin failing (or timing out), the circuit trips open, and the backend automatically falls back to serving the last successfully cached database stats without attempting to hit the broken external APIs.

### E. WebClient Connection Pooling
* **The Strategy**: Default HTTP clients create and destroy TCP connections constantly, which wastes CPU cycles.
* **Optimization**: Configure the underlying reactor-netty library to use a fixed Connection Pool:
  ```java
  ConnectionProvider provider = ConnectionProvider.builder("custom-pool")
          .maxConnections(50)
          .pendingAcquireTimeout(Duration.ofSeconds(20))
          .build();
  ```
  Reusing existing TCP connections drastically reduces API latency.

