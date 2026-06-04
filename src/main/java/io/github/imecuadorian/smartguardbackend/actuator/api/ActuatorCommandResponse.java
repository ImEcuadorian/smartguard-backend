package io.github.imecuadorian.smartguardbackend.actuator.api;

import io.github.imecuadorian.smartguardbackend.actuator.domain.ActuatorCommandStatus;
import io.github.imecuadorian.smartguardbackend.actuator.domain.ActuatorCommandType;

import java.time.Instant;
import java.util.UUID;

public record ActuatorCommandResponse(
        UUID id,
        UUID actuatorId,
        UUID deviceId,
        ActuatorCommandType command,
        ActuatorCommandStatus status,
        String payload,
        Instant createdAt,
        Instant sentAt
) {
}
