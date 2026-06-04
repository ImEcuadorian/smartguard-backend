package io.github.imecuadorian.smartguardbackend.monitoring.api;

import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.Instant;

public record CreateSensorReadingRequest(
        BigDecimal numericValue,
        Boolean booleanValue,

        @Size(max = 255, message = "Text value must have at most 255 characters")
        String textValue,

        Instant recordedAt
) {
}
