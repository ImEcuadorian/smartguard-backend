package io.github.imecuadorian.smartguardbackend.actuator.api;

import io.github.imecuadorian.smartguardbackend.actuator.domain.ActuatorType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CreateActuatorRequest(
        @NotNull(message = "Device id is required")
        UUID deviceId,

        @NotBlank(message = "Actuator code is required")
        @Size(max = 80, message = "Actuator code must have at most 80 characters")
        String code,

        @NotBlank(message = "Actuator name is required")
        @Size(max = 120, message = "Actuator name must have at most 120 characters")
        String name,

        @NotNull(message = "Actuator type is required")
        ActuatorType type,

        @Size(max = 160, message = "Actuator location must have at most 160 characters")
        String location
) {
}
