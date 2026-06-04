package io.github.imecuadorian.smartguardbackend.alert.api;

import io.github.imecuadorian.smartguardbackend.alert.domain.AlertSeverity;
import io.github.imecuadorian.smartguardbackend.alert.domain.AlertStatus;
import io.github.imecuadorian.smartguardbackend.alert.domain.AlertType;

import java.time.Instant;
import java.util.UUID;

public record AlertResponse(
        UUID id,
        UUID deviceId,
        UUID sensorId,
        AlertType type,
        AlertSeverity severity,
        AlertStatus status,
        String message,
        Instant occurredAt,
        Instant createdAt,
        Instant acknowledgedAt,
        Instant resolvedAt
) {
}
