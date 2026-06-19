# Sensor Rules Alert Automation Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add configurable sensor rules that create automatic alerts from readings, support historical reading queries for charts, and detect sensors that stop reporting.

**Architecture:** Keep the feature inside the existing `monitoring` module, with a small application service for rule evaluation. `SensorService` remains the ingestion boundary for REST and MQTT readings, while `AlertService` owns alert creation/deduplication and WebSocket publication.

**Tech Stack:** Spring Boot 4, Spring MVC, Spring Data JPA, Jakarta Validation, WebSocket/STOMP, JUnit 5, Mockito.

---

### Task 1: Sensor Rule Domain And API

**Files:**
- Create: `src/main/java/io/github/imecuadorian/smartguardbackend/monitoring/domain/SensorAlertRule.java`
- Create: `src/main/java/io/github/imecuadorian/smartguardbackend/monitoring/domain/SensorAlertRuleType.java`
- Create: `src/main/java/io/github/imecuadorian/smartguardbackend/monitoring/domain/ComparisonOperator.java`
- Create: `src/main/java/io/github/imecuadorian/smartguardbackend/monitoring/infrastructure/SensorAlertRuleRepository.java`
- Create API request/response/mapper classes under `monitoring/api`
- Modify: `SensorController`
- Test: `SensorAlertRuleServiceTest`, `SensorControllerTest`

- [ ] Write failing tests for creating/listing/disabling rules.
- [ ] Implement rule entity, repository, service, DTOs, and endpoints.
- [ ] Run focused monitoring tests.

### Task 2: Automatic Alert Evaluation

**Files:**
- Create: `src/main/java/io/github/imecuadorian/smartguardbackend/monitoring/application/SensorAlertRuleEvaluator.java`
- Modify: `AlertService`
- Modify: `AlertRepository`
- Modify: `SensorService`
- Test: `SensorAlertRuleEvaluatorTest`, `SensorServiceTest`

- [ ] Write failing tests for numeric threshold, emergency boolean, and duplicate alert prevention.
- [ ] Implement evaluator and alert deduplication.
- [ ] Invoke evaluator after every reading from REST or MQTT.

### Task 3: Historical Reading Queries

**Files:**
- Modify: `SensorReadingRepository`
- Modify: `SensorService`
- Modify: `SensorController`
- Test: `SensorServiceTest`, `SensorControllerTest`

- [ ] Write failing tests for `from`, `to`, `limit`, and latest reading.
- [ ] Implement bounded query parameters and latest endpoint.
- [ ] Keep default behavior backward compatible.

### Task 4: Sensor Silence Detection

**Files:**
- Modify: `Sensor`
- Modify: `SmartguardBackendApplication`
- Create: `src/main/java/io/github/imecuadorian/smartguardbackend/monitoring/application/SensorSilenceMonitor.java`
- Modify: `application.yaml`
- Test: `SensorSilenceMonitorTest`

- [ ] Write failing tests for no-reading alerts when a sensor is silent beyond configured minutes.
- [ ] Track `lastReadingAt` on each sensor.
- [ ] Add scheduled monitor guarded by configuration.

### Task 5: Docs And Verification

**Files:**
- Modify: `docs/postman/smartguard-backend.postman_collection.json`
- Modify: `docs/postman/README.md`

- [ ] Add Postman requests for alert rules, filtered readings, latest reading, and MQTT examples.
- [ ] Run `.\gradlew.bat clean build --offline`.
- [ ] Verify Docker/health if the build passes.
