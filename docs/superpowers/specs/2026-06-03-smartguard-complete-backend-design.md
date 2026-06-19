# SmartGuard Complete Backend Design

## Goal

Build the SmartGuard backend as a professional academic IoT platform with REST APIs, PostgreSQL persistence, MQTT ingestion/commands, WebSocket live updates, JWT security, device credentials, Docker support, and Postman-ready workflows.

## Scope

The backend will support:

- Devices: ESP32 registration and status.
- Monitoring: sensors and sensor readings.
- Access control: RFID readers, RFID cards, and access events.
- Actuators: relays, servos, buzzers, LEDs, and commands.
- Alerts: important events derived from readings, access control, or device status.
- MQTT: inbound telemetry and outbound device commands.
- WebSocket: live updates for web and mobile clients.
- Security: users, roles, JWT access tokens, and first-admin bootstrap.
- Docker: backend service enabled for production-like runs with Doppler-provided environment variables.

## Architecture

Use package-by-domain modules:

```txt
device
monitoring
access
actuator
alert
mqtt
realtime
security
config
shared
```

Each domain follows:

```txt
api             REST controllers and DTO records
application     use cases/services
domain          entities, enums, domain rules
infrastructure  repositories and external adapters
```

Controllers stay thin. Services hold business rules. Repositories are Spring Data JPA interfaces. REST responses use DTOs, never entities.

## Data Flow

Sensor telemetry:

```txt
ESP32 -> MQTT readings topic -> backend handler -> SensorReading -> Alert rules -> WebSocket topic
```

RFID scan:

```txt
ESP32 -> MQTT access topic -> backend validates card -> AccessEvent -> optional ActuatorCommand -> MQTT command topic -> WebSocket topic
```

Manual actuator command:

```txt
Web/Postman -> REST command endpoint -> ActuatorCommand -> MQTT command topic -> WebSocket topic
```

## Security

REST APIs are protected by JWT except health/info and auth/bootstrap endpoints. Roles:

```txt
ADMIN
OPERATOR
VIEWER
```

The first administrator can be created only when no user accounts exist. Passwords are stored with BCrypt. Device MQTT/REST trust is prepared with device keys, but MQTT broker authentication remains configured outside the app through Doppler/Mosquitto.

## Error Handling

All API errors return the existing structured error response with status, message, path, timestamp, and validation map.

## Testing

Use TDD with:

- Controller slice tests for REST contracts.
- Service unit tests for rules.
- Context tests with JPA excluded to avoid needing PostgreSQL during local test runs.
- MQTT/WebSocket service tests at the message composition layer.

## Production Readiness Boundaries

This backend will be production-shaped for an academic project. It will not include advanced production features such as distributed tracing, database migrations with Flyway, refresh token rotation storage, rate limiting, or a hardened public signup flow unless added later.
