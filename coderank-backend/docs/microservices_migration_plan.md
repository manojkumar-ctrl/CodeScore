# Microservices Migration Plan for CodeRank

Migrating from a modular monolith to a microservices architecture is a journey. It's best done **incrementally** (often called the "Strangler Fig" pattern) rather than rewriting everything from scratch all at once.

Here is the step-by-step plan to transition your `coderank-backend` into microservices using the **Spring Cloud** ecosystem.

---

## Phase 1: Preparation & Infrastructure

Before splitting the code, we need the infrastructure to support multiple services.

### 1. Set up an API Gateway
- **Action:** Create a brand new Spring Boot project (e.g., `coderank-gateway`).
- **Dependencies:** `Spring Cloud Gateway`.
- **Purpose:** The frontend (`client-coderank`) will stop calling your backend directly. Instead, it will call the Gateway (e.g., on port 8080). The Gateway will route requests to your monolithic backend (e.g., on port 8081).
- **Benefit:** When you extract a microservice later, the frontend doesn't need to change its URLs; the Gateway will just reroute the specific endpoint to the new microservice.

### 2. Set up Service Discovery (Optional but Recommended)
- **Action:** Create another Spring Boot project (e.g., `coderank-eureka-server`).
- **Dependencies:** `Eureka Server`.
- **Purpose:** Microservices register themselves here. The API Gateway will ask Eureka "Where is the Analysis Service?" instead of hardcoding IP addresses.

---

## Phase 2: Extracting the First Microservice

Always start with a service that has the **fewest dependencies** on other modules. In CodeRank, `Analysis` or `Profile` (Leetcode/Github fetching) are great candidates. Let's extract `Profile`.

### 1. Create the `profile-service`
- **Action:** Create a new Spring Boot app: `profile-service`.
- **Move Code:** Move everything inside `com.coderank.profile` from the monolith to this new project.
- **Database Separation:** Give `profile-service` its own database schema or an entirely separate Postgres instance. Migrate the `leetcode_stats` and `github_stats` tables here.

### 2. Implement Inter-Service Communication
Now, the monolith (which still contains `User` and `Analysis`) needs profile data, but it's no longer in the same app!
- **Action:** In the monolith, use `Spring Cloud OpenFeign` or `WebClient`.
- **Code Change:** Instead of `profileRepository.findByUserId(...)`, the monolith makes an HTTP GET request: `http://profile-service/api/profiles/{userId}`.

### 3. Update the API Gateway
- **Action:** Configure the Gateway to route `/api/profiles/**` to the new `profile-service`, and let everything else still go to the monolith.

---

## Phase 3: Handling Authentication

Authentication is tricky in microservices. You don't want every service to individually check the database for the user.

### 1. Extract the `auth-service`
- **Action:** Move the `auth` and `user` modules into an `auth-service` (or separate them into `auth-service` and `user-service`).
- **Mechanism:** The `auth-service` is the only one that accesses the `users` table and generates the JWT.

### 2. Stateless JWT Validation
- **Action:** Ensure your JWTs contain all necessary claims (like `userId` and `roles`).
- **Implementation:** Other services (like `profile-service` and `analysis-service`) do *not* call `auth-service` to validate a token. Instead, they use a shared secret key (or public key) to cryptographically verify the JWT themselves. Alternatively, you can do token validation at the API Gateway level, and simply pass the `X-User-Id` header to downstream services.

---

## Phase 4: Extracting the Core Engine

### 1. Create the `analysis-service`
- **Action:** Extract the `analysis` module into its own application.
- **Challenges:** The `AnalysisService` likely needs `User` data and `Profile` data simultaneously to generate leaderboards.
- **Solution:** It will make concurrent REST calls (via WebClient or Feign) to `user-service` and `profile-service`, aggregate the data, and run its algorithms.

---

## Summary of the Final Architecture

Once complete, your workspace will transform from one backend project to several:

1.  **`coderank-gateway`** (Port 8080) - Routes traffic.
2.  **`coderank-eureka`** (Port 8761) - Service registry.
3.  **`coderank-auth-service`** (Port 8081) - Login & Users (Has DB 1).
4.  **`coderank-profile-service`** (Port 8082) - GitHub/LeetCode fetching (Has DB 2).
5.  **`coderank-analysis-service`** (Port 8083) - Leaderboards & Comparisons (Has DB 3).

## Next Steps for You Right Now

To actually begin this:
1. Would you like me to generate a `pom.xml` and main class for the **API Gateway** to start Phase 1?
2. Or would you like to see how we refactor the code inside `AnalysisService.java` to use a `WebClient` for network calls instead of local database calls?
