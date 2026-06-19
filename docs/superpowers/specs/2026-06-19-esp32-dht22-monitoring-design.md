# ESP32 DHT22 Monitoring Design

**Date:** 2026-06-19

## Goal

Validate the complete SmartGuard path with one physical DHT22 connected to a 30-pin ESP32:

```txt
DHT22 -> ESP32 -> Wi-Fi -> Mosquitto -> Spring Boot -> PostgreSQL
      -> WebSocket/STOMP -> Next.js dashboard
```

## Scope

- Read temperature and relative humidity from one DHT22.
- Publish both measurements through the existing MQTT contract.
- Store and display readings without changing backend or frontend contracts.
- Verify historical charts and real-time invalidation in Next.js.
- Keep Wi-Fi, MQTT and device credentials outside version control.

Access control, actuators, RFID and additional physical sensors are outside this test.

## Domain Mapping

One physical DHT22 is represented by two SmartGuard sensor records because each record has one type, unit, history and independent WebSocket topic:

| Sensor code | Type | Unit | MQTT value |
|---|---|---|---|
| `temp-dht22-001` | `TEMPERATURE` | `celsius` | `numericValue` |
| `humidity-dht22-001` | `HUMIDITY` | `%` | `numericValue` |

Both sensors belong to device `esp32-dht22-001`.

## Hardware

Target board: ESP32 development board with 30 pins.

| DHT22 pin | ESP32 connection |
|---|---|
| VCC | 3.3 V |
| DATA | GPIO 4 |
| GND | GND |

For a bare four-pin DHT22, place a 10 kOhm pull-up resistor between DATA and 3.3 V. A three-pin DHT22 module normally includes this resistor.

## Firmware Structure

The Arduino sketch will use:

- ESP32 `WiFi` library.
- `PubSubClient` for MQTT.
- Adafruit `DHT sensor library` and `Adafruit Unified Sensor`.
- `ArduinoJson` for safe JSON construction.

Configuration is split into:

- `smartguard_dht22_monitor.ino`: firmware logic, safe to commit.
- `secrets.example.h`: placeholder template, safe to commit.
- `secrets.h`: local Wi-Fi, broker, MQTT and device credentials, ignored by Git.

The sketch will reconnect Wi-Fi and MQTT automatically, read every 10 seconds, reject `NaN` values, and publish temperature and humidity separately. It will omit `recordedAt` so the backend assigns its own timestamp and the ESP32 does not require NTP for this test.

## MQTT Contract

Broker host must be the LAN IPv4 address of the PC running Mosquitto. `localhost` cannot be used from the ESP32 because it would refer to the ESP32 itself.

Topic:

```txt
smartguard/devices/esp32-dht22-001/readings
```

Temperature payload:

```json
{
  "deviceCode": "esp32-dht22-001",
  "deviceApiKey": "DEVICE_API_KEY_FROM_POSTMAN",
  "sensorCode": "temp-dht22-001",
  "numericValue": 24.6
}
```

Humidity uses the same topic with `sensorCode: humidity-dht22-001`.

The MQTT username/password used by the ESP32 and backend must exist in Mosquitto's password file. Doppler configures the backend client but does not create Mosquitto users.

## Provisioning Flow

With an empty database, Postman will perform:

1. Bootstrap the first administrator and save access/refresh tokens.
2. Create device `esp32-dht22-001` and save the one-time `apiKey`.
3. Create temperature sensor `temp-dht22-001` for that device.
4. Create humidity sensor `humidity-dht22-001` for that device.
5. Optionally create numeric threshold rules after basic readings work.

The device API key is copied into local `secrets.h`; it is not a user JWT.

## Frontend Flow

The existing frontend contract requires no change:

- Sensor list loads both logical sensors through REST.
- Sensor detail loads latest and historical readings.
- It subscribes to `/topic/sensors/{sensorId}/readings` using the JWT query parameter.
- Each MQTT reading causes the backend to persist data and publish the WebSocket event.
- React Query invalidates the selected sensor's reading query and refreshes its chart.

Temperature and humidity appear as two sensor detail pages. A combined DHT22 dashboard can be added later without changing firmware or backend contracts.

## Error Handling

- Invalid DHT readings are logged and not published.
- Wi-Fi and MQTT reconnection use bounded retry intervals.
- MQTT publish failures remain visible in Serial Monitor.
- Unknown sensor code, wrong device API key or topic/device mismatch is rejected by the backend.
- The firmware prints connection state but never prints passwords or the device API key.

## Test Criteria

The test succeeds when:

1. ESP32 Serial Monitor reports Wi-Fi and MQTT connected.
2. Temperature and humidity publishes return success.
3. PostgreSQL-backed REST endpoints return both latest readings.
4. Next.js sensor pages update without manual browser refresh.
5. Historical charts contain new numeric samples.
6. Disconnecting the DHT22 produces no fabricated readings.

## Security Notes

- No real credentials are committed.
- `.gitignore` must include the firmware `secrets.h` file.
- The PC firewall must allow inbound TCP 1883 only on the trusted local network.
- Credentials visible in screenshots or chat should be rotated before any public deployment.
