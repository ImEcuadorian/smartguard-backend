# Professional Device Foundation Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build a professional Spring Boot foundation for SmartGuard with a production-shaped `Device` REST module ready for Postman testing.

**Architecture:** Use a modular package-by-domain structure with thin REST controllers, application services, domain entities/enums, infrastructure repositories, shared API error handling, and explicit DTOs. Keep Doppler as the external configuration source and avoid committing secrets.

**Tech Stack:** Spring Boot 4, Spring MVC, Spring Data JPA, Spring Security, Bean Validation, PostgreSQL for runtime, H2 for isolated tests, JUnit 5, MockMvc.

---

### Task 1: Test Baseline

**Files:**
- Modify: `build.gradle.kts`
- Modify: `src/test/java/io/github/imecuadorian/smartguardbackend/SmartguardBackendApplicationTests.java`

- [ ] Add H2 as a test runtime dependency.
- [ ] Configure the application context test to use an in-memory database.
- [ ] Run `.\gradlew.bat test` and confirm the context test reaches the next real failure or passes.

### Task 2: Device REST Behavior

**Files:**
- Create: `src/test/java/io/github/imecuadorian/smartguardbackend/device/api/DeviceControllerTest.java`
- Create: `src/main/java/io/github/imecuadorian/smartguardbackend/device/...`
- Create: `src/main/java/io/github/imecuadorian/smartguardbackend/shared/...`

- [ ] Write MockMvc tests first for creating, listing, fetching, updating, changing status, validation errors, and missing devices.
- [ ] Verify tests fail because the API does not exist.
- [ ] Implement Device DTOs, domain entity, enum, repository, service, mapper, and controller.
- [ ] Add shared global exception handling with consistent API error responses.
- [ ] Run Device tests and confirm they pass.

### Task 3: Security And Configuration

**Files:**
- Create: `src/main/java/io/github/imecuadorian/smartguardbackend/security/config/SecurityConfig.java`
- Modify: `src/main/resources/application.yaml`

- [ ] Add a stateless security configuration with CSRF disabled for REST APIs.
- [ ] Permit actuator health/info and the current MVP REST API for Postman development.
- [ ] Keep JWT configuration values externalized through Doppler.
- [ ] Add JPA auditing support.

### Task 4: Postman Readiness

**Files:**
- Create: `docs/postman/smartguard-backend.postman_collection.json`

- [ ] Add a Postman collection for the `Device` endpoints.
- [ ] Run the full Gradle test suite.
- [ ] Report exact commands for running with Doppler and testing from Postman.
