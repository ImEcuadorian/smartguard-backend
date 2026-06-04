package io.github.imecuadorian.smartguardbackend.mqtt.api;

import java.math.BigDecimal;
import java.time.Instant;

public record MqttSensorReadingPayload(
        String deviceCode,
        String deviceApiKey,
        String sensorCode,
        BigDecimal numericValue,
        Boolean booleanValue,
        String textValue,
        Instant recordedAt
) {
}
