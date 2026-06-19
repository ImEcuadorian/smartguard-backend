# Production Readiness Backend Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Finish the backend hardening pass needed before sharing the project with frontend and uploading to GitHub.

**Architecture:** Keep authentication/session logic inside `security`, keep OpenAPI documentation as a local controller to avoid adding unavailable Swagger dependencies, and add Flyway migrations while preserving current Docker/dev compatibility. RBAC is enforced at HTTP security rules, with JWT propagated to REST and WebSocket handshakes.

**Tech Stack:** Spring Boot 4, Spring Security, Spring Data JPA, Flyway, PostgreSQL, MQTT Mosquitto, STOMP WebSocket, JUnit 5, Mockito.

---

### Task 1: Refresh Tokens And `/me`

**Files:**
- Create `security/domain/RefreshToken.java`
- Create `security/infrastructure/RefreshTokenRepository.java`
- Create auth DTOs for refresh, logout, current user and password change
- Modify `AuthService`, `AuthController`, `AuthResponse`, `UserAccount`
- Test `AuthServiceTest`, `JwtServiceTest`

- [x] Add opaque refresh tokens stored as SHA-256 hashes with expiration and revocation.
- [x] Return `refreshToken` on bootstrap/login/refresh.
- [x] Add `POST /api/v1/auth/refresh`, `POST /api/v1/auth/logout`, `GET /api/v1/auth/me`, `PATCH /api/v1/auth/me/password`.

### Task 2: Users And RBAC

**Files:**
- Create `security/api/UserController.java`
- Create user admin DTOs
- Create `security/application/UserService.java`
- Modify `SecurityConfig`
- Test `SecurityConfigTest`, `UserServiceTest`

- [x] Admin can create/list/disable users.
- [x] Viewer can read only.
- [x] Operator can perform operational writes but not admin/user/device management.
- [x] Admin keeps full access.

### Task 3: Flyway

**Files:**
- Modify `build.gradle.kts`
- Create `src/main/resources/db/migration/V1__smartguard_initial_schema.sql`
- Modify `application.yaml`, `.env.example`, `compose.yaml`

- [x] Add Flyway dependencies available in Gradle cache.
- [x] Add migration SQL for current schema.
- [x] Keep dev Docker compatible with existing volume and allow `JPA_DDL_AUTO=update` for local dev.

### Task 4: OpenAPI Documentation

**Files:**
- Create `docs/api/OpenApiDocumentController.java`
- Test controller health via build.

- [x] Expose `GET /api-docs/openapi.json`.
- [x] Expose `GET /api-docs` as a simple HTML landing page.
- [x] Permit those endpoints without JWT.

### Task 5: WebSocket And MQTT Hardening

**Files:**
- Modify `JwtAuthenticationFilter`, `SecurityConfig`, `docker/mosquitto/mosquitto.conf`, `compose.yaml`, `.env.example`

- [x] Allow WebSocket JWT through `?access_token=...`.
- [x] Require auth for `/ws`.
- [x] Configure Mosquitto with username/password in dev.

### Task 6: Filters And Docs

**Files:**
- Modify repositories/services/controllers for devices, sensors, alerts and access events.
- Modify Postman/frontend docs.

- [x] Add list filters requested by frontend.
- [x] Update integration report with refresh token, RBAC, OpenAPI and WebSocket token usage.
- [x] Run full tests, Docker build and health verification.
