package io.github.imecuadorian.smartguardbackend.mqtt.application;

import io.github.imecuadorian.smartguardbackend.mqtt.api.MqttActuatorCommandPayload;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.integration.mqtt.support.MqttHeaders;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Component
public class MqttCommandPublisher {

    private final MessageChannel mqttOutboundChannel;
    private final ObjectMapper objectMapper;

    public MqttCommandPublisher(@Qualifier("mqttOutboundChannel") MessageChannel mqttOutboundChannel,
                                ObjectMapper objectMapper) {
        this.mqttOutboundChannel = mqttOutboundChannel;
        this.objectMapper = objectMapper;
    }

    public void publish(String topic, MqttActuatorCommandPayload payload) {
        try {
            mqttOutboundChannel.send(MessageBuilder.withPayload(objectMapper.writeValueAsString(payload))
                    .setHeader(MqttHeaders.TOPIC, topic)
                    .build());
        } catch (Exception exception) {
            throw new IllegalArgumentException("Could not publish MQTT command", exception);
        }
    }
}
