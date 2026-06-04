package io.github.imecuadorian.smartguardbackend.mqtt.api;

import io.github.imecuadorian.smartguardbackend.actuator.domain.ActuatorCommandType;

import java.util.UUID;

public record MqttActuatorCommandPayload(
        UUID commandId,
        String actuatorCode,
        ActuatorCommandType command,
        String payload
) {
}
