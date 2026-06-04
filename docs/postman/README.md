# SmartGuard Backend Testing

## Local services

Start PostgreSQL and Mosquitto for local development:

```powershell
docker compose up -d postgres mosquitto
```

Run the backend with Doppler dev:

```powershell
doppler run --config dev -- .\gradlew.bat bootRun
```

Run the full stack in Docker with Doppler prd:

```powershell
doppler run --config prd -- docker compose up -d --build
```

The API is available at:

```txt
http://localhost:8080
```

## Postman collection

Import:

```txt
docs/postman/smartguard-backend.postman_collection.json
```

Recommended order:

1. `Auth / Bootstrap Admin` once on a clean database.
2. `Auth / Login` on later runs.
3. `Auth / Me` to confirm the JWT.
4. `Users / Create Operator User` if you want to test RBAC with another account.
5. `Devices / Create Device`.
6. `Sensors / Create Sensor`.
7. `Sensors / Create Gas Threshold Rule`.
8. `Sensors / Create No Reading Rule`.
9. `Access RFID / Create Access Reader`.
10. `Access RFID / Create RFID Card`.
11. `Actuators / Create Actuator`.
12. Continue with readings, scans, commands and alerts.
13. `Auth / Refresh Token` to rotate access/refresh tokens.
14. `Auth / Logout` when finishing the session.

The collection saves these variables automatically:

```txt
accessToken
refreshToken
userId
deviceId
deviceCode
deviceApiKey
sensorId
sensorAlertRuleId
readerId
cardId
actuatorId
alertId
```

## Auth and roles

`Bootstrap Admin`, `Login` and `Refresh Token` save `accessToken` and `refreshToken` automatically.

Use this header for protected endpoints:

```txt
Authorization: Bearer {{accessToken}}
```

Role behavior:

```txt
ADMIN: full management, including users and device setup.
OPERATOR: operational writes such as readings, scans, commands and alert handling.
VIEWER: read-only dashboard access.
```

Current-user endpoints:

```txt
GET   /api/v1/auth/me
PATCH /api/v1/auth/me/password
POST  /api/v1/auth/refresh
POST  /api/v1/auth/logout
```

Admin user management:

```txt
POST  /api/v1/users
GET   /api/v1/users
PATCH /api/v1/users/{id}/status
```

## Sensor rules

Create numeric threshold rule:

```json
{
  "type": "NUMERIC_THRESHOLD",
  "operator": "GREATER_THAN",
  "thresholdValue": 700,
  "expectedBooleanValue": null,
  "durationMinutes": null,
  "alertType": "GAS_DETECTED",
  "severity": "CRITICAL",
  "message": "Gas level exceeded 700 ppm"
}
```

Create no-reading rule:

```json
{
  "type": "NO_READING",
  "operator": null,
  "thresholdValue": null,
  "expectedBooleanValue": null,
  "durationMinutes": 5,
  "alertType": "DEVICE_OFFLINE",
  "severity": "WARNING",
  "message": "Sensor stopped reporting for 5 minutes"
}
```

When a reading triggers a rule, the backend creates an alert automatically and publishes it through WebSocket.

Historical readings for charts:

```txt
GET /api/v1/sensors/{sensorId}/readings?from=2026-06-04T00:00:00Z&to=2026-06-04T23:59:59Z&limit=50
GET /api/v1/sensors/{sensorId}/readings/latest
```

Other useful filters:

```txt
GET /api/v1/devices?status=ACTIVE
GET /api/v1/sensors?deviceId={deviceId}&status=ACTIVE&type=GAS
GET /api/v1/alerts?status=OPEN&severity=CRITICAL
GET /api/v1/access/events?from=2026-06-04T00:00:00Z&to=2026-06-04T23:59:59Z&limit=100
```

## MQTT payloads

The backend subscribes to:

```txt
smartguard/devices/+/readings
smartguard/devices/+/access-events
```

Sensor reading example:

```json
{
  "deviceCode": "esp32-001",
  "deviceApiKey": "{{deviceApiKey}}",
  "sensorCode": "gas-main",
  "numericValue": 25.5,
  "booleanValue": null,
  "textValue": null,
  "recordedAt": "2026-06-03T20:00:00Z"
}
```

If `numericValue` is greater than a configured threshold, or a boolean/duration/no-reading rule matches, an alert is created automatically.

Access event example:

```json
{
  "deviceCode": "esp32-001",
  "deviceApiKey": "{{deviceApiKey}}",
  "readerCode": "rfid-main",
  "cardUid": "A1:B2:C3:D4",
  "occurredAt": "2026-06-03T20:00:00Z"
}
```

Actuator commands are published by the backend to:

```txt
smartguard/devices/{deviceCode}/commands
```

## WebSocket topics

Connect to:

```txt
ws://localhost:8080/ws?access_token={{accessToken}}
```

Useful STOMP subscriptions:

```txt
/topic/sensors/{sensorId}/readings
/topic/devices/{deviceId}/readings
/topic/access/events
/topic/devices/{deviceId}/access-events
/topic/actuators/{actuatorId}/commands
/topic/devices/{deviceId}/commands
/topic/alerts
/topic/devices/{deviceId}/alerts
```

## Production notes

OpenAPI:

```txt
GET /api-docs
GET /api-docs/openapi.json
```

Before treating `prd` as real production, update Doppler so production does not use localhost CORS, `APP_ENV=development`, debug logs, empty MQTT credentials, or the same secrets as dev.

For Docker Mosquitto dev, use:

```txt
MQTT_USERNAME=smartguard
MQTT_PASSWORD=smartguard_mqtt_dev_password
```

For a fresh production database after Flyway is established, prefer:

```txt
JPA_DDL_AUTO=validate
FLYWAY_ENABLED=true
```
