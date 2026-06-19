# SmartGuard Complete Backend Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Complete the SmartGuard backend with monitoring, access control, actuators, alerts, MQTT, WebSocket, JWT security, Docker, and Postman support.

**Architecture:** Extend the existing package-by-domain structure. Each module gets DTOs, controller, service, entity/enums, repository, focused tests, and integration hooks only where needed.

**Tech Stack:** Spring Boot 4, Spring MVC, Spring Data JPA, Spring Security, WebSocket/STOMP, Spring Integration MQTT, PostgreSQL, Doppler, Docker Compose, JUnit 5, Mockito, MockMvc.

---

### Task 1: Monitoring

**Files:**
- Create `src/main/java/io/github/imecuadorian/smartguardbackend/monitoring/**`
- Create `src/test/java/io/github/imecuadorian/smartguardbackend/monitoring/**`

- [ ] Add Sensor and SensorReading enums, entities, DTOs, repository interfaces, mapper, service, and controller.
- [ ] Add service tests for duplicate sensor code, missing sensor, and reading creation.
- [ ] Add controller tests for creating sensors, listing sensors, creating readings, and validation errors.
- [ ] Run focused tests.

### Task 2: Access Control

**Files:**
- Create `src/main/java/io/github/imecuadorian/smartguardbackend/access/**`
- Create `src/test/java/io/github/imecuadorian/smartguardbackend/access/**`

- [ ] Add AccessReader, RfidCard, and AccessEvent model.
- [ ] Add service rules for granted/denied card scans.
- [ ] Add REST endpoints for readers, cards, and scan events.
- [ ] Add tests for granted and denied access.

### Task 3: Actuators

**Files:**
- Create `src/main/java/io/github/imecuadorian/smartguardbackend/actuator/**`
- Create `src/test/java/io/github/imecuadorian/smartguardbackend/actuator/**`

- [ ] Add Actuator and ActuatorCommand model.
- [ ] Add REST endpoints to register actuators and enqueue commands.
- [ ] Add service tests for command creation and duplicate actuator code.

### Task 4: Alerts

**Files:**
- Create `src/main/java/io/github/imecuadorian/smartguardbackend/alert/**`
- Create `src/test/java/io/github/imecuadorian/smartguardbackend/alert/**`

- [ ] Add Alert model and REST endpoints.
- [ ] Add acknowledgement and resolution rules.
- [ ] Add tests for alert lifecycle.

### Task 5: Realtime WebSocket

**Files:**
- Create `src/main/java/io/github/imecuadorian/smartguardbackend/realtime/**`

- [ ] Add STOMP endpoint and broker configuration from application properties.
- [ ] Add a RealtimeNotificationService used by domain services.
- [ ] Publish readings, access events, commands, and alerts.

### Task 6: MQTT

**Files:**
- Create `src/main/java/io/github/imecuadorian/smartguardbackend/mqtt/**`

- [ ] Add MQTT properties.
- [ ] Add inbound topics for readings, access scans, and device status.
- [ ] Add outbound command publisher.
- [ ] Add message handler tests without requiring a live broker.

### Task 7: JWT Security

**Files:**
- Create `src/main/java/io/github/imecuadorian/smartguardbackend/security/**`
- Modify `src/main/java/io/github/imecuadorian/smartguardbackend/security/config/SecurityConfig.java`

- [ ] Add user account entity, repository, role/status enums, auth DTOs, auth service, JWT service, and authentication filter.
- [ ] Add bootstrap-admin endpoint that only works while no users exist.
- [ ] Protect APIs by default and keep health/info/auth endpoints public.
- [ ] Add tests for login and protected API behavior.

### Task 8: Docker And Postman

**Files:**
- Modify `compose.yaml`
- Create/modify `docs/postman/**`

- [ ] Enable backend compose service.
- [ ] Add `.env.example` with non-secret placeholders.
- [ ] Expand Postman collection for all modules.
- [ ] Run full tests and build.
