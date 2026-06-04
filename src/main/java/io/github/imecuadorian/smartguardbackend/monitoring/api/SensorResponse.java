package io.github.imecuadorian.smartguardbackend.monitoring.api;

import io.github.imecuadorian.smartguardbackend.monitoring.domain.SensorStatus;
import io.github.imecuadorian.smartguardbackend.monitoring.domain.SensorType;

import java.time.Instant;
import java.util.UUID;

public record SensorResponse(
        UUID id,
        UUID deviceId,
        String code,
        String name,
        SensorType type,
        String unit,
        String location,
        SensorStatus status,
        Instant lastReadingAt,
        Instant createdAt,
        Instant updatedAt
) {
}
