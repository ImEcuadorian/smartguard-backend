package io.github.imecuadorian.smartguardbackend.monitoring.api;

import io.github.imecuadorian.smartguardbackend.monitoring.domain.SensorType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CreateSensorRequest(
        @NotNull(message = "Device id is required")
        UUID deviceId,

        @NotBlank(message = "Sensor code is required")
        @Size(max = 80, message = "Sensor code must have at most 80 characters")
        String code,

        @NotBlank(message = "Sensor name is required")
        @Size(max = 120, message = "Sensor name must have at most 120 characters")
        String name,

        @NotNull(message = "Sensor type is required")
        SensorType type,

        @Size(max = 40, message = "Sensor unit must have at most 40 characters")
        String unit,

        @Size(max = 160, message = "Sensor location must have at most 160 characters")
        String location
) {
}
