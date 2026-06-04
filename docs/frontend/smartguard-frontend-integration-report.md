# SmartGuard Frontend Integration Report

Fecha: 2026-06-04

Este documento es la guia de integracion para los equipos de frontend web y movil. El backend ya expone REST API, JWT con refresh token, RBAC, MQTT para ESP32, WebSocket/STOMP con JWT, reglas automaticas de sensores, alertas, Flyway y OpenAPI.

## 1. Estado Del Backend

Verificacion realizada antes de entregar este informe:

```txt
Backend Docker health: UP
Gradle tests offline: BUILD SUCCESSFUL
Protected API without JWT: 401
Postman collection JSON: valid
OpenAPI: GET /api-docs
```

Archivos utiles:

```txt
docs/postman/smartguard-backend.postman_collection.json
docs/postman/README.md
.env.example
compose.yaml
```

## 2. URLs Base

Web Next.js en la misma PC:

```env
NEXT_PUBLIC_API_URL=http://localhost:8080
NEXT_PUBLIC_WS_URL=ws://localhost:8080/ws
```

Expo en emulador Android:

```env
EXPO_PUBLIC_API_URL=http://10.0.2.2:8080
EXPO_PUBLIC_WS_URL=ws://10.0.2.2:8080/ws
```

Expo en celular fisico:

```env
EXPO_PUBLIC_API_URL=http://IP_DE_TU_PC:8080
EXPO_PUBLIC_WS_URL=ws://IP_DE_TU_PC:8080/ws
```

Nota: `localhost` en un celular apunta al celular, no a la PC.

## 3. Seguridad

No requieren token:

```txt
GET  /actuator/health
GET  /api-docs
GET  /api-docs/openapi.json
POST /api/v1/auth/bootstrap-admin
POST /api/v1/auth/login
POST /api/v1/auth/refresh
POST /api/v1/auth/logout
```

Todo lo demas requiere access token:

```txt
Authorization: Bearer <accessToken>
```

Bootstrap admin solo funciona cuando no existen usuarios. Despues usar login.

Roles por endpoint:

```txt
ADMIN: usuarios, crear/editar devices, crear/editar sensores, reglas, lectores, tarjetas, actuadores, alertas y comandos.
OPERATOR: operacion diaria, lecturas, escaneos, comandos, alertas y mantenimiento de sensores.
VIEWER: lectura de dashboards/listas/detalles, sin mutaciones.
```

El frontend debe guardar `accessToken` y `refreshToken`. Cuando una request regrese `401`, intenten `POST /api/v1/auth/refresh` con el refresh token; si falla, cerrar sesion.

## 4. Axios Base

### Next.js App Router

Instalar:

```powershell
npm i axios @tanstack/react-query @stomp/stompjs
```

`src/lib/api/http.ts`:

```ts
import axios from "axios";

export class ApiError extends Error {
  constructor(
    message: string,
    public status: number,
    public validation?: Record<string, string>,
  ) {
    super(message);
    this.name = "ApiError";
  }
}

export const api = axios.create({
  baseURL: process.env.NEXT_PUBLIC_API_URL,
  headers: { "Content-Type": "application/json" },
});

api.interceptors.request.use((config) => {
  if (typeof window !== "undefined") {
    const token = window.localStorage.getItem("smartguard.accessToken");
    if (token) config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

api.interceptors.response.use(
  (response) => response,
  (error) => {
    const data = error.response?.data;
    throw new ApiError(
      data?.message ?? "Request failed",
      error.response?.status ?? 0,
      data?.validation,
    );
  },
);
```

Importante para Next.js App Router:

```txt
Usar este cliente en Client Components, hooks o servicios llamados desde cliente.
Si quieren SSR con auth, conviene mover el token a cookie httpOnly y crear API routes proxy.
Para el MVP academico, localStorage + Client Components es suficiente.
```

### Expo

Instalar:

```powershell
npm i axios @tanstack/react-query @stomp/stompjs
npx expo install expo-secure-store
```

`src/lib/api/http.ts`:

```ts
import axios from "axios";
import * as SecureStore from "expo-secure-store";

export class ApiError extends Error {
  constructor(
    message: string,
    public status: number,
    public validation?: Record<string, string>,
  ) {
    super(message);
    this.name = "ApiError";
  }
}

export const api = axios.create({
  baseURL: process.env.EXPO_PUBLIC_API_URL,
  headers: { "Content-Type": "application/json" },
});

api.interceptors.request.use(async (config) => {
  const token = await SecureStore.getItemAsync("smartguard.accessToken");
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});

api.interceptors.response.use(
  (response) => response,
  (error) => {
    const data = error.response?.data;
    throw new ApiError(
      data?.message ?? "Request failed",
      error.response?.status ?? 0,
      data?.validation,
    );
  },
);
```

## 5. React Query Provider

Next.js `src/app/providers.tsx`:

```tsx
"use client";

import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { useState } from "react";

export function Providers({ children }: { children: React.ReactNode }) {
  const [queryClient] = useState(
    () =>
      new QueryClient({
        defaultOptions: {
          queries: { staleTime: 30_000, retry: 1 },
          mutations: { retry: 0 },
        },
      }),
  );

  return <QueryClientProvider client={queryClient}>{children}</QueryClientProvider>;
}
```

Expo `app/_layout.tsx`:

```tsx
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { Stack } from "expo-router";
import { useState } from "react";

export default function RootLayout() {
  const [queryClient] = useState(() => new QueryClient());

  return (
    <QueryClientProvider client={queryClient}>
      <Stack />
    </QueryClientProvider>
  );
}
```

## 6. Tipos TypeScript Compartidos

Crear `src/lib/api/types.ts` en web y movil, o compartirlo en un package comun.

```ts
export type UUID = string;
export type ISODateTime = string;

export type UserRole = "ADMIN" | "OPERATOR" | "VIEWER";
export type DeviceStatus = "ACTIVE" | "INACTIVE" | "MAINTENANCE";
export type SensorStatus = "ACTIVE" | "INACTIVE" | "MAINTENANCE";
export type SensorType =
  | "TEMPERATURE"
  | "HUMIDITY"
  | "GAS"
  | "MOTION"
  | "DOOR"
  | "LIGHT"
  | "DISTANCE"
  | "EMERGENCY_BUTTON";

export type ComparisonOperator =
  | "GREATER_THAN"
  | "GREATER_THAN_OR_EQUAL"
  | "LESS_THAN"
  | "LESS_THAN_OR_EQUAL"
  | "EQUAL";

export type SensorAlertRuleType =
  | "NUMERIC_THRESHOLD"
  | "BOOLEAN_MATCH"
  | "DURATION_OPEN"
  | "NO_READING";

export type AlertType =
  | "GAS_DETECTED"
  | "MOTION_DETECTED"
  | "DOOR_OPEN"
  | "EMERGENCY_BUTTON"
  | "ACCESS_DENIED"
  | "DEVICE_OFFLINE"
  | "THRESHOLD_EXCEEDED";

export type AlertSeverity = "INFO" | "WARNING" | "CRITICAL";
export type AlertStatus = "OPEN" | "ACKNOWLEDGED" | "RESOLVED";

export type AccessReaderType = "RFID_RC522" | "NFC_PN532";
export type AccessReaderStatus = "ACTIVE" | "INACTIVE" | "MAINTENANCE";
export type RfidCardStatus = "ACTIVE" | "BLOCKED" | "INACTIVE";
export type AccessResult = "GRANTED" | "DENIED";

export type ActuatorType = "RELAY" | "SERVO" | "BUZZER" | "LED" | "SOLENOID_LOCK";
export type ActuatorStatus = "ACTIVE" | "INACTIVE" | "MAINTENANCE";
export type ActuatorCommandType =
  | "OPEN_DOOR"
  | "CLOSE_DOOR"
  | "LOCK"
  | "UNLOCK"
  | "TURN_ON"
  | "TURN_OFF"
  | "BEEP";
export type ActuatorCommandStatus = "PENDING" | "SENT" | "FAILED";

export interface ApiErrorResponse {
  timestamp: ISODateTime;
  status: number;
  error: string;
  message: string;
  path: string;
  validation: Record<string, string>;
}

export interface AuthResponse {
  tokenType: "Bearer";
  accessToken: string;
  refreshToken: string;
  expiresInMinutes: number;
  username: string;
  role: UserRole;
}

export type UserStatus = "ACTIVE" | "DISABLED";

export interface UserAccountResponse {
  id: UUID;
  username: string;
  displayName: string;
  role: UserRole;
  status: UserStatus;
  createdAt: ISODateTime | null;
  updatedAt: ISODateTime | null;
}

export interface DeviceResponse {
  id: UUID;
  code: string;
  name: string;
  location: string | null;
  status: DeviceStatus;
  ipAddress: string | null;
  firmwareVersion: string | null;
  lastSeenAt: ISODateTime | null;
  createdAt: ISODateTime | null;
  updatedAt: ISODateTime | null;
}

export interface DeviceRegistrationResponse {
  device: DeviceResponse;
  apiKey: string;
}

export interface SensorResponse {
  id: UUID;
  deviceId: UUID;
  code: string;
  name: string;
  type: SensorType;
  unit: string | null;
  location: string | null;
  status: SensorStatus;
  lastReadingAt: ISODateTime | null;
  createdAt: ISODateTime | null;
  updatedAt: ISODateTime | null;
}

export interface SensorReadingResponse {
  id: UUID;
  sensorId: UUID;
  deviceId: UUID;
  numericValue: number | null;
  booleanValue: boolean | null;
  textValue: string | null;
  recordedAt: ISODateTime;
  createdAt: ISODateTime | null;
}

export interface SensorAlertRuleResponse {
  id: UUID;
  sensorId: UUID;
  type: SensorAlertRuleType;
  operator: ComparisonOperator | null;
  thresholdValue: number | null;
  expectedBooleanValue: boolean | null;
  durationMinutes: number | null;
  alertType: AlertType;
  severity: AlertSeverity;
  message: string;
  enabled: boolean;
  createdAt: ISODateTime | null;
  updatedAt: ISODateTime | null;
}

export interface AlertResponse {
  id: UUID;
  deviceId: UUID | null;
  sensorId: UUID | null;
  type: AlertType;
  severity: AlertSeverity;
  status: AlertStatus;
  message: string;
  occurredAt: ISODateTime;
  createdAt: ISODateTime | null;
  acknowledgedAt: ISODateTime | null;
  resolvedAt: ISODateTime | null;
}

export interface AccessReaderResponse {
  id: UUID;
  deviceId: UUID;
  code: string;
  type: AccessReaderType;
  location: string | null;
  status: AccessReaderStatus;
  createdAt: ISODateTime | null;
  updatedAt: ISODateTime | null;
}

export interface RfidCardResponse {
  id: UUID;
  uid: string;
  ownerName: string;
  status: RfidCardStatus;
  createdAt: ISODateTime | null;
  updatedAt: ISODateTime | null;
}

export interface AccessEventResponse {
  id: UUID;
  deviceId: UUID;
  readerId: UUID;
  cardId: UUID | null;
  cardUid: string;
  result: AccessResult;
  reason: string;
  occurredAt: ISODateTime;
  createdAt: ISODateTime | null;
}

export interface ActuatorResponse {
  id: UUID;
  deviceId: UUID;
  code: string;
  name: string;
  type: ActuatorType;
  location: string | null;
  status: ActuatorStatus;
  createdAt: ISODateTime | null;
  updatedAt: ISODateTime | null;
}

export interface ActuatorCommandResponse {
  id: UUID;
  actuatorId: UUID;
  deviceId: UUID;
  command: ActuatorCommandType;
  status: ActuatorCommandStatus;
  payload: string | null;
  createdAt: ISODateTime | null;
  sentAt: ISODateTime | null;
}
```

## 7. API Services

Crear `src/lib/api/smartguard-api.ts`:

```ts
import { api } from "./http";
import type {
  AccessEventResponse,
  AccessReaderResponse,
  ActuatorCommandResponse,
  ActuatorCommandType,
  ActuatorResponse,
  AlertResponse,
  AlertSeverity,
  AlertStatus,
  AlertType,
  AuthResponse,
  ComparisonOperator,
  DeviceRegistrationResponse,
  DeviceResponse,
  DeviceStatus,
  SensorAlertRuleResponse,
  SensorAlertRuleType,
  SensorReadingResponse,
  SensorResponse,
  SensorStatus,
  SensorType,
  UserAccountResponse,
  UserRole,
  UserStatus,
  UUID,
} from "./types";

export const authApi = {
  bootstrapAdmin: (body: { username: string; password: string; displayName: string }) =>
    api.post<AuthResponse>("/api/v1/auth/bootstrap-admin", body).then((r) => r.data),

  login: (body: { username: string; password: string }) =>
    api.post<AuthResponse>("/api/v1/auth/login", body).then((r) => r.data),

  refresh: (refreshToken: string) =>
    api.post<AuthResponse>("/api/v1/auth/refresh", { refreshToken }).then((r) => r.data),

  logout: (refreshToken: string) =>
    api.post<void>("/api/v1/auth/logout", { refreshToken }).then((r) => r.data),

  me: () => api.get<UserAccountResponse>("/api/v1/auth/me").then((r) => r.data),

  changePassword: (body: { currentPassword: string; newPassword: string }) =>
    api.patch<UserAccountResponse>("/api/v1/auth/me/password", body).then((r) => r.data),
};

export const userApi = {
  create: (body: { username: string; password: string; displayName: string; role: UserRole }) =>
    api.post<UserAccountResponse>("/api/v1/users", body).then((r) => r.data),

  list: () => api.get<UserAccountResponse[]>("/api/v1/users").then((r) => r.data),

  updateStatus: (id: UUID, status: UserStatus) =>
    api.patch<UserAccountResponse>(`/api/v1/users/${id}/status`, { status }).then((r) => r.data),
};

export const deviceApi = {
  create: (body: {
    code: string;
    name: string;
    location?: string | null;
    ipAddress?: string | null;
    firmwareVersion?: string | null;
  }) => api.post<DeviceRegistrationResponse>("/api/v1/devices", body).then((r) => r.data),

  list: (params?: { status?: DeviceStatus }) =>
    api.get<DeviceResponse[]>("/api/v1/devices", { params }).then((r) => r.data),
  get: (id: UUID) => api.get<DeviceResponse>(`/api/v1/devices/${id}`).then((r) => r.data),
  updateStatus: (id: UUID, status: DeviceStatus) =>
    api.patch<DeviceResponse>(`/api/v1/devices/${id}/status`, { status }).then((r) => r.data),
};

export const sensorApi = {
  create: (body: {
    deviceId: UUID;
    code: string;
    name: string;
    type: SensorType;
    unit?: string | null;
    location?: string | null;
  }) => api.post<SensorResponse>("/api/v1/sensors", body).then((r) => r.data),

  list: (params?: { deviceId?: UUID; status?: SensorStatus; type?: SensorType }) =>
    api.get<SensorResponse[]>("/api/v1/sensors", { params }).then((r) => r.data),
  get: (id: UUID) => api.get<SensorResponse>(`/api/v1/sensors/${id}`).then((r) => r.data),
  updateStatus: (id: UUID, status: SensorStatus) =>
    api.patch<SensorResponse>(`/api/v1/sensors/${id}/status`, { status }).then((r) => r.data),

  createReading: (
    id: UUID,
    body: {
      numericValue?: number | null;
      booleanValue?: boolean | null;
      textValue?: string | null;
      recordedAt?: string | null;
    },
  ) => api.post<SensorReadingResponse>(`/api/v1/sensors/${id}/readings`, body).then((r) => r.data),

  readings: (id: UUID, params?: { from?: string; to?: string; limit?: number }) =>
    api.get<SensorReadingResponse[]>(`/api/v1/sensors/${id}/readings`, { params }).then((r) => r.data),

  latestReading: (id: UUID) =>
    api.get<SensorReadingResponse>(`/api/v1/sensors/${id}/readings/latest`).then((r) => r.data),

  createRule: (
    id: UUID,
    body: {
      type: SensorAlertRuleType;
      operator?: ComparisonOperator | null;
      thresholdValue?: number | null;
      expectedBooleanValue?: boolean | null;
      durationMinutes?: number | null;
      alertType: AlertType;
      severity: AlertSeverity;
      message: string;
    },
  ) => api.post<SensorAlertRuleResponse>(`/api/v1/sensors/${id}/alert-rules`, body).then((r) => r.data),

  rules: (id: UUID) =>
    api.get<SensorAlertRuleResponse[]>(`/api/v1/sensors/${id}/alert-rules`).then((r) => r.data),

  disableRule: (ruleId: UUID) =>
    api.patch<SensorAlertRuleResponse>(`/api/v1/sensor-alert-rules/${ruleId}/disable`).then((r) => r.data),
};

export const alertApi = {
  list: (params?: { status?: AlertStatus; severity?: AlertSeverity }) =>
    api.get<AlertResponse[]>("/api/v1/alerts", { params }).then((r) => r.data),
  acknowledge: (id: UUID) => api.patch<AlertResponse>(`/api/v1/alerts/${id}/acknowledge`).then((r) => r.data),
  resolve: (id: UUID) => api.patch<AlertResponse>(`/api/v1/alerts/${id}/resolve`).then((r) => r.data),
};

export const accessApi = {
  readers: () => api.get<AccessReaderResponse[]>("/api/v1/access/readers").then((r) => r.data),
  events: (params?: { from?: string; to?: string; limit?: number }) =>
    api.get<AccessEventResponse[]>("/api/v1/access/events", { params }).then((r) => r.data),
};

export const actuatorApi = {
  list: () => api.get<ActuatorResponse[]>("/api/v1/actuators").then((r) => r.data),
  sendCommand: (id: UUID, command: ActuatorCommandType, payload?: string | null) =>
    api.post<ActuatorCommandResponse>(`/api/v1/actuators/${id}/commands`, { command, payload }).then((r) => r.data),
  commands: (id: UUID) =>
    api.get<ActuatorCommandResponse[]>(`/api/v1/actuators/${id}/commands`).then((r) => r.data),
};
```

## 8. Endpoints REST

### Auth

| Metodo | Endpoint | Auth | Uso |
|---|---|---|---|
| POST | `/api/v1/auth/bootstrap-admin` | No | Crear primer admin |
| POST | `/api/v1/auth/login` | No | Login y obtener access/refresh token |
| POST | `/api/v1/auth/refresh` | No | Renovar sesion |
| POST | `/api/v1/auth/logout` | No | Revocar refresh token |
| GET | `/api/v1/auth/me` | Si | Perfil actual |
| PATCH | `/api/v1/auth/me/password` | Si | Cambiar contrasena propia |

### Users

Solo `ADMIN`.

| Metodo | Endpoint | Uso |
|---|---|---|
| POST | `/api/v1/users` | Crear operador/admin/viewer |
| GET | `/api/v1/users` | Listar usuarios |
| PATCH | `/api/v1/users/{id}/status` | Activar/desactivar usuario |

### Devices

| Metodo | Endpoint | Uso |
|---|---|---|
| POST | `/api/v1/devices` | Crear ESP32 y obtener `apiKey` una sola vez |
| GET | `/api/v1/devices?status=` | Listar dispositivos con filtro opcional |
| GET | `/api/v1/devices/{id}` | Detalle |
| PATCH | `/api/v1/devices/{id}` | Editar datos |
| PATCH | `/api/v1/devices/{id}/status` | Cambiar estado |

Nota: `apiKey` solo aparece al crear el dispositivo. El frontend debe mostrarla/copiarla para configurar el ESP32.

### Sensors

| Metodo | Endpoint | Uso |
|---|---|---|
| POST | `/api/v1/sensors` | Crear sensor |
| GET | `/api/v1/sensors?deviceId=&status=&type=` | Listar sensores con filtros opcionales |
| GET | `/api/v1/sensors/{id}` | Detalle sensor |
| PATCH | `/api/v1/sensors/{id}/status` | Cambiar estado |
| POST | `/api/v1/sensors/{id}/readings` | Crear lectura manual |
| GET | `/api/v1/sensors/{id}/readings?from=&to=&limit=` | Historial para graficas |
| GET | `/api/v1/sensors/{id}/readings/latest` | Ultima lectura |
| POST | `/api/v1/sensors/{id}/alert-rules` | Crear regla |
| GET | `/api/v1/sensors/{id}/alert-rules` | Listar reglas |
| PATCH | `/api/v1/sensor-alert-rules/{id}` | Editar regla |
| PATCH | `/api/v1/sensor-alert-rules/{id}/disable` | Desactivar regla |

Regla gas > 700:

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

Regla sensor sin lecturas por 5 minutos:

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

### Access RFID

| Metodo | Endpoint | Uso |
|---|---|---|
| POST | `/api/v1/access/readers` | Crear lector RFID/NFC |
| GET | `/api/v1/access/readers` | Listar lectores |
| POST | `/api/v1/access/cards` | Crear tarjeta RFID |
| GET | `/api/v1/access/cards` | Listar tarjetas |
| POST | `/api/v1/access/events/scan` | Simular/registrar escaneo |
| GET | `/api/v1/access/events?from=&to=&limit=` | Historial accesos |

### Actuators

| Metodo | Endpoint | Uso |
|---|---|---|
| POST | `/api/v1/actuators` | Crear actuador |
| GET | `/api/v1/actuators` | Listar actuadores |
| POST | `/api/v1/actuators/{id}/commands` | Enviar comando |
| GET | `/api/v1/actuators/{id}/commands` | Historial comandos |

Comando ejemplo:

```json
{
  "command": "UNLOCK",
  "payload": "{\"durationSeconds\":5}"
}
```

### Alerts

| Metodo | Endpoint | Uso |
|---|---|---|
| POST | `/api/v1/alerts` | Crear alerta manual |
| GET | `/api/v1/alerts?status=&severity=` | Listar alertas con filtros opcionales |
| PATCH | `/api/v1/alerts/{id}/acknowledge` | Reconocer alerta |
| PATCH | `/api/v1/alerts/{id}/resolve` | Resolver alerta |

## 9. WebSocket/STOMP

El backend usa STOMP sobre WebSocket directo, sin SockJS.

Instalar:

```powershell
npm i @stomp/stompjs
```

Cliente:

```ts
import { Client } from "@stomp/stompjs";

export function createRealtimeClient(accessToken: string, onConnect: (client: Client) => void) {
  const wsUrl = process.env.NEXT_PUBLIC_WS_URL ?? process.env.EXPO_PUBLIC_WS_URL;

  const client = new Client({
    brokerURL: `${wsUrl}?access_token=${encodeURIComponent(accessToken)}`,
    reconnectDelay: 5000,
    onConnect: () => onConnect(client),
    onStompError: (frame) => console.error("STOMP error", frame),
  });

  client.activate();
  return () => client.deactivate();
}
```

Topicos disponibles:

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

Ejemplo:

```ts
createRealtimeClient(accessToken, (client) => {
  client.subscribe("/topic/alerts", (message) => {
    const alert = JSON.parse(message.body) as AlertResponse;
    console.log("Nueva alerta", alert);
  });
});
```

## 10. MQTT Para ESP32

El frontend normalmente no publica MQTT. Esto lo usa ESP32 y backend. El broker Mosquitto requiere usuario/contrasena en Docker; en Doppler deben existir `MQTT_USERNAME` y `MQTT_PASSWORD`.

Topic de lecturas:

```txt
smartguard/devices/{deviceCode}/readings
```

Payload:

```json
{
  "deviceCode": "esp32-001",
  "deviceApiKey": "API_KEY_DEL_DEVICE",
  "sensorCode": "gas-main",
  "numericValue": 750,
  "booleanValue": null,
  "textValue": null,
  "recordedAt": "2026-06-04T01:00:00Z"
}
```

Topic eventos RFID:

```txt
smartguard/devices/{deviceCode}/access-events
```

Payload:

```json
{
  "deviceCode": "esp32-001",
  "deviceApiKey": "API_KEY_DEL_DEVICE",
  "readerCode": "rfid-main",
  "cardUid": "A1:B2:C3:D4",
  "occurredAt": "2026-06-04T01:00:00Z"
}
```

Comandos de actuadores publicados por backend:

```txt
smartguard/devices/{deviceCode}/commands
```

## 11. Pantallas Recomendadas

### Next.js App Router

```txt
src/app/(auth)/login/page.tsx
src/app/(dashboard)/layout.tsx
src/app/(dashboard)/page.tsx
src/app/(dashboard)/devices/page.tsx
src/app/(dashboard)/devices/[id]/page.tsx
src/app/(dashboard)/sensors/page.tsx
src/app/(dashboard)/sensors/[id]/page.tsx
src/app/(dashboard)/access/page.tsx
src/app/(dashboard)/actuators/page.tsx
src/app/(dashboard)/alerts/page.tsx
src/app/(dashboard)/settings/page.tsx
```

Uso por pantalla:

```txt
Dashboard: resumen, ultimas lecturas, alertas abiertas, eventos recientes.
Devices: CRUD y estado de ESP32.
Sensors: listado, reglas, ultima lectura.
Sensor detail: grafica historica, reglas, lecturas recientes.
Access: lectores, tarjetas, eventos granted/denied.
Actuators: comandos manuales y estado.
Alerts: reconocer/resolver y filtros por severidad.
```

### Expo Router

```txt
app/(auth)/login.tsx
app/(tabs)/index.tsx
app/(tabs)/alerts.tsx
app/(tabs)/devices.tsx
app/(tabs)/sensors.tsx
app/(tabs)/control.tsx
app/(tabs)/settings.tsx
app/sensors/[id].tsx
app/devices/[id].tsx
```

Uso movil:

```txt
Home: estado del sistema.
Alerts: prioridad movil.
Devices: estado rapido de ESP32.
Sensors: ultimas lecturas.
Control: actuadores importantes.
Settings: perfil y sesion.
```

## 12. Tailwind/UI

Para Next.js usar Tailwind normal. Para Expo usar NativeWind si quieren clases Tailwind en React Native.

Recomendacion visual:

```txt
Dashboard profesional, denso y claro.
Cards solo para items repetidos.
Tablas para web, listas compactas para movil.
Colores por severidad:
INFO: neutral/blue
WARNING: amber
CRITICAL: red
ACTIVE: green
INACTIVE: gray
MAINTENANCE: amber
```

## 13. Flujo De Pruebas Frontend

1. Importar Postman y validar que backend responde.
2. Hacer `Bootstrap Admin` en base limpia.
3. Login y guardar `accessToken` y `refreshToken`.
4. Crear device y guardar `apiKey` del device.
5. Crear sensor `gas-main`.
6. Crear regla `GAS > 700`.
7. Crear lectura manual con `numericValue: 750`.
8. Confirmar que aparece alerta en `GET /api/v1/alerts`.
9. Conectar WebSocket a `/topic/alerts`.
10. Conectar WebSocket usando `?access_token=<accessToken>`.
11. Repetir lectura y confirmar evento en vivo.

## 14. Pendientes Que El Frontend Debe Decidir

```txt
Manejo de sesion: localStorage/SecureStore para MVP o cookies httpOnly para SSR.
Formato de graficas: line chart para numericValue, timeline para booleanValue.
UX de device apiKey: mostrar solo al crear device y pedir confirmacion de copiado.
Filtros: ya existen en backend para devices, sensores, alertas, access events y lecturas.
```

## 15. OpenAPI

Documentacion rapida:

```txt
GET /api-docs
GET /api-docs/openapi.json
```

Esto sirve para que el equipo frontend revise rutas desde el navegador y pueda generar tipos si despues quieren automatizarlo.
