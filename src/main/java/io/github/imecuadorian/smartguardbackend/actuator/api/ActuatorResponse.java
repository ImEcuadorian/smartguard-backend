package io.github.imecuadorian.smartguardbackend.actuator.api;

import io.github.imecuadorian.smartguardbackend.actuator.domain.ActuatorStatus;
import io.github.imecuadorian.smartguardbackend.actuator.domain.ActuatorType;

import java.time.Instant;
import java.util.UUID;

public record ActuatorResponse(
        UUID id,
        UUID deviceId,
        String code,
        String name,
        ActuatorType type,
        String location,
        ActuatorStatus status,
        Instant createdAt,
        Instant updatedAt
) {
}
