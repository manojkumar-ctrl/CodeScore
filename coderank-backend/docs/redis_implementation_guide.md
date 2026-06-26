# Redis Caching Implementation Guide for CodeRank

We have successfully integrated **Redis** into CodeRank using the **Cache-Aside Pattern**. This document outlines exactly where the changes were made, how the caching works, and how to verify it.

---

## 1. Caching Architecture (Cache-Aside Pattern)

The dashboard and leaderboards are read-heavy resources. Pulling them from PostgreSQL on every page load wastes database power. Here is how the implemented caching system handles queries:

```
[ Dashboard Request ]
         │
         ├──► 1. Check Redis Cache (Is "dashboard::user_id" there?)
         │         │
         │         ├──► [ YES: Cache HIT ] ──────────────┐
         │         │    Return cached JSON data          │
         │         │    Time: ~5ms - 20ms                │
         │         │                                     ▼
         │         └──► [ NO: Cache MISS ] ────► [ Return Response ]
         │              1. Query PostgreSQL              ▲
         │              2. Sync GitHub/LeetCode APIs     │
         │              3. Save response in Redis        │
         │              4. Time: ~300ms - 800ms ─────────┘
         │
[ Refresh Request ]
         │
         └──► 1. Evict (delete) "dashboard::user_id" key in Redis
              2. Fetch fresh APIs, recalculate, and save to Postgres
              3. Next dashboard request naturally triggers a Cache MISS to rebuild it
```

---

## 2. Where Redis was Integrated: Code & File Changes

Here is a full breakdown of every file modified or created to support Redis:

### A. Dependencies & Activation
*   **File:** [`pom.xml`](file:///c:/Users/Manojkumar/OneDrive/Desktop/THE%20BIG%20ONE/coderank-backend/pom.xml)
    *   *Change:* Added `spring-boot-starter-data-redis` to add Redis Client connection support.
*   **File:** [`CoderankBackendApplication.java`](file:///c:/Users/Manojkumar/OneDrive/Desktop/THE%20BIG%20ONE/coderank-backend/src/main/java/com/coderank/CoderankBackendApplication.java)
    *   *Change:* Added `@EnableCaching` on the main application class to activate Spring Cache.

### B. Redis Configuration
*   **File (New):** [`RedisConfig.java`](file:///c:/Users/Manojkumar/OneDrive/Desktop/THE%20BIG%20ONE/coderank-backend/src/main/java/com/coderank/config/RedisConfig.java)
    *   *Role:* Overrides default binary Java serializers. It sets key serializers as plain strings and values as JSON (using `GenericJackson2JsonRedisSerializer`). This ensures that cached entries are human-readable when inspected via the command line or UI tools.
*   **File:** [`application.properties`](file:///c:/Users/Manojkumar/OneDrive/Desktop/THE%20BIG%20ONE/coderank-backend/src/main/resources/application.properties)
    *   *Change:* Added properties to read `${REDIS_HOST}` and `${REDIS_PORT}` with defaults, configured a default TTL of 10 minutes (600,000 ms), and disabled the Hibernate `open-in-view` warnings.
*   **File:** [`.env`](file:///c:/Users/Manojkumar/OneDrive/Desktop/THE%20BIG%20ONE/coderank-backend/.env)
    *   *Change:* Added `REDIS_HOST=localhost` and `REDIS_PORT=6379` for local development.

### C. Service Caching Logic
*   **File:** [`AnalysisService.java`](file:///c:/Users/Manojkumar/OneDrive/Desktop/THE%20BIG%20ONE/coderank-backend/src/main/java/com/coderank/analysis/service/AnalysisService.java)
    *   **`getDashboard`**: Annotated with `@Cacheable(value = "dashboard", key = "#user.id")`. Caches the dashboard object per user ID.
    *   **`getLeaderboard`**: Annotated with `@Cacheable(value = "leaderboard", key = "#category")`. Caches leaderboards per category (e.g., dsa, contest, github).
    *   **`runAnalysis`**: Annotated with `@CacheEvict(value = "dashboard", key = "#user.id")`. Whenever user statistics are refreshed or re-calculated, the old cache is destroyed to prevent stale data.

### D. Infrastructure (Multi-Container Run)
*   **File:** [`docker-compose.yml`](file:///c:/Users/Manojkumar/OneDrive/Desktop/THE%20BIG%20ONE/docker-compose.yml)
    *   *Change:* Added the `redis` container service using the lightweight `redis:7-alpine` image.
    *   *Change:* Injected an environment override `REDIS_HOST: redis` to the backend container. Because they are on the same virtual bridge network (`coderank-net`), Docker resolves `redis` directly to the caching service.

---

## 3. How to Verify the Caching works

You can test this easily using the **Redis CLI** inside your running container:

1.  Make sure your docker containers are running:
    ```powershell
    docker compose up --build
    ```
2.  Open a new terminal tab and connect to the Redis CLI inside the container:
    ```powershell
    docker exec -it coderank-redis redis-cli
    ```
3.  Type `KEYS *` to list cached entries. Initially, it will be empty:
    ```
    127.0.0.1:6379> KEYS *
    (empty array)
    ```
4.  Open the frontend or trigger the API (`http://localhost:8080/api/analysis/dashboard`).
5.  Type `KEYS *` again. You will see the cache populated:
    ```
    127.0.0.1:6379> KEYS *
    1) "dashboard::1"
    ```
6.  Query the key directly to view the JSON stored inside Redis:
    ```
    127.0.0.1:6379> GET "dashboard::1"
    "{\"@class\":\"com.coderank.analysis.dto.DashboardResponse\",...}"
    ```
7.  Check the TTL (expiration timer):
    ```
    127.0.0.1:6379> TTL "dashboard::1"
    (integer) 584  # Seconds remaining until automatic expiration
    ```
