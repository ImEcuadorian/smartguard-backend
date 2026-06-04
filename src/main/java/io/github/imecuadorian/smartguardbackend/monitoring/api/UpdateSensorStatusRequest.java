package io.github.imecuadorian.smartguardbackend.monitoring.api;

import io.github.imecuadorian.smartguardbackend.monitoring.domain.SensorStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateSensorStatusRequest(
        @NotNull(message = "Sensor status is required")
        SensorStatus status
) {
}
