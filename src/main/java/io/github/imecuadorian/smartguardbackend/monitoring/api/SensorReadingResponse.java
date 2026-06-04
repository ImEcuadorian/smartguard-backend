package io.github.imecuadorian.smartguardbackend.monitoring.api;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record SensorReadingResponse(
        UUID id,
        UUID sensorId,
        UUID deviceId,
        BigDecimal numericValue,
        Boolean booleanValue,
        String textValue,
        Instant recordedAt,
        Instant createdAt
) {
}
