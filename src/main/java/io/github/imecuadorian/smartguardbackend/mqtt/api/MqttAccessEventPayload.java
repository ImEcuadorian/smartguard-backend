package io.github.imecuadorian.smartguardbackend.mqtt.api;

import java.time.Instant;

public record MqttAccessEventPayload(
        String deviceCode,
        String deviceApiKey,
        String readerCode,
        String cardUid,
        Instant occurredAt
) {
}
