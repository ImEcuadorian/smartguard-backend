package io.github.imecuadorian.smartguardbackend.docs.api;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
public class OpenApiDocumentController {

    @GetMapping(value = "/api-docs/openapi.json", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> openApi() {
        return Map.of(
                "openapi", "3.1.0",
                "info", Map.of(
                        "title", "SmartGuard Backend API",
                        "version", "0.0.1",
                        "description", "REST API for SmartGuard devices, sensors, RFID access, actuators and alerts."
                ),
                "servers", List.of(Map.of("url", "http://localhost:8080")),
                "security", List.of(Map.of("bearerAuth", List.of())),
                "components", Map.of(
                        "securitySchemes", Map.of(
                                "bearerAuth", Map.of(
                                        "type", "http",
                                        "scheme", "bearer",
                                        "bearerFormat", "JWT"
                                )
                        )
                ),
                "paths", paths()
        );
    }

    @GetMapping(value = "/api-docs", produces = MediaType.TEXT_HTML_VALUE)
    public String html() {
        return """
                <!doctype html>
                <html lang="en">
                <head>
                  <meta charset="utf-8">
                  <title>SmartGuard API Docs</title>
                  <style>
                    body { font-family: system-ui, sans-serif; margin: 2rem; line-height: 1.5; }
                    code, pre { background: #f4f4f5; padding: .15rem .3rem; border-radius: .25rem; }
                  </style>
                </head>
                <body>
                  <h1>SmartGuard API Docs</h1>
                  <p>OpenAPI JSON: <a href="/api-docs/openapi.json">/api-docs/openapi.json</a></p>
                  <p>Use this JSON with Swagger Editor, Bruno, Insomnia, Postman or frontend codegen.</p>
                </body>
                </html>
                """;
    }

    private Map<String, Object> paths() {
        Map<String, Object> paths = new LinkedHashMap<>();
        addPath(paths, "POST", "/api/v1/auth/bootstrap-admin", "Bootstrap first admin");
        addPath(paths, "POST", "/api/v1/auth/login", "Login");
        addPath(paths, "POST", "/api/v1/auth/refresh", "Refresh access token");
        addPath(paths, "POST", "/api/v1/auth/logout", "Revoke refresh token");
        addPath(paths, "GET", "/api/v1/auth/me", "Current authenticated user");
        addPath(paths, "PATCH", "/api/v1/auth/me/password", "Change current user password");
        addPath(paths, "GET", "/api/v1/users", "List users");
        addPath(paths, "POST", "/api/v1/users", "Create user");
        addPath(paths, "PATCH", "/api/v1/users/{id}/status", "Update user status");
        addPath(paths, "GET", "/api/v1/devices", "List devices");
        addPath(paths, "POST", "/api/v1/devices", "Create device");
        addPath(paths, "GET", "/api/v1/devices/{id}", "Get device");
        addPath(paths, "PATCH", "/api/v1/devices/{id}", "Update device");
        addPath(paths, "PATCH", "/api/v1/devices/{id}/status", "Update device status");
        addPath(paths, "GET", "/api/v1/sensors", "List sensors");
        addPath(paths, "POST", "/api/v1/sensors", "Create sensor");
        addPath(paths, "GET", "/api/v1/sensors/{id}", "Get sensor");
        addPath(paths, "PATCH", "/api/v1/sensors/{id}/status", "Update sensor status");
        addPath(paths, "GET", "/api/v1/sensors/{id}/readings", "List sensor readings");
        addPath(paths, "POST", "/api/v1/sensors/{id}/readings", "Create sensor reading");
        addPath(paths, "GET", "/api/v1/sensors/{id}/readings/latest", "Get latest sensor reading");
        addPath(paths, "GET", "/api/v1/sensors/{id}/alert-rules", "List sensor alert rules");
        addPath(paths, "POST", "/api/v1/sensors/{id}/alert-rules", "Create sensor alert rule");
        addPath(paths, "PATCH", "/api/v1/sensor-alert-rules/{id}", "Update sensor alert rule");
        addPath(paths, "PATCH", "/api/v1/sensor-alert-rules/{id}/disable", "Disable sensor alert rule");
        addPath(paths, "GET", "/api/v1/access/readers", "List access readers");
        addPath(paths, "POST", "/api/v1/access/readers", "Create access reader");
        addPath(paths, "GET", "/api/v1/access/cards", "List RFID cards");
        addPath(paths, "POST", "/api/v1/access/cards", "Create RFID card");
        addPath(paths, "GET", "/api/v1/access/events", "List access events");
        addPath(paths, "POST", "/api/v1/access/events/scan", "Register access scan");
        addPath(paths, "GET", "/api/v1/actuators", "List actuators");
        addPath(paths, "POST", "/api/v1/actuators", "Create actuator");
        addPath(paths, "GET", "/api/v1/actuators/{id}/commands", "List actuator commands");
        addPath(paths, "POST", "/api/v1/actuators/{id}/commands", "Send actuator command");
        addPath(paths, "GET", "/api/v1/alerts", "List alerts");
        addPath(paths, "POST", "/api/v1/alerts", "Create alert");
        addPath(paths, "PATCH", "/api/v1/alerts/{id}/acknowledge", "Acknowledge alert");
        addPath(paths, "PATCH", "/api/v1/alerts/{id}/resolve", "Resolve alert");
        return paths;
    }

    @SuppressWarnings("unchecked")
    private void addPath(Map<String, Object> paths, String method, String path, String summary) {
        Map<String, Object> operations = (Map<String, Object>) paths.computeIfAbsent(path, ignored -> new LinkedHashMap<>());
        operations.put(method.toLowerCase(), Map.of(
                "summary", summary,
                "responses", Map.of(
                        "200", Map.of("description", "OK"),
                        "400", Map.of("description", "Bad Request"),
                        "401", Map.of("description", "Unauthorized"),
                        "403", Map.of("description", "Forbidden"),
                        "404", Map.of("description", "Not Found")
                )
        ));
    }
}
