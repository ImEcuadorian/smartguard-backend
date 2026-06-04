package io.github.imecuadorian.smartguardbackend.alert.api;

import io.github.imecuadorian.smartguardbackend.alert.domain.AlertSeverity;
import io.github.imecuadorian.smartguardbackend.alert.domain.AlertType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.UUID;

public record CreateAlertRequest(
        UUID deviceId,
        UUID sensorId,

        @NotNull(message = "Alert type is required")
        AlertType type,

        @NotNull(message = "Alert severity is required")
        AlertSeverity severity,

        @NotBlank(message = "Alert message is required")
        @Size(max = 255, message = "Alert message must have at most 255 characters")
        String message,

        Instant occurredAt
) {
}
