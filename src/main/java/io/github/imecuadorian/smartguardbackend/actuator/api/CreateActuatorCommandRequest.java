package io.github.imecuadorian.smartguardbackend.actuator.api;

import io.github.imecuadorian.smartguardbackend.actuator.domain.ActuatorCommandType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateActuatorCommandRequest(
        @NotNull(message = "Actuator command is required")
        ActuatorCommandType command,

        @Size(max = 255, message = "Actuator command payload must have at most 255 characters")
        String payload
) {
}
