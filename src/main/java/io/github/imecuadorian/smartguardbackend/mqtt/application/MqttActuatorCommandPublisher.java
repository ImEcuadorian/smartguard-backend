package io.github.imecuadorian.smartguardbackend.mqtt.application;

import io.github.imecuadorian.smartguardbackend.actuator.application.ActuatorCommandPublisher;
import io.github.imecuadorian.smartguardbackend.actuator.domain.ActuatorCommand;
import io.github.imecuadorian.smartguardbackend.mqtt.api.MqttActuatorCommandPayload;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "smartguard.mqtt.enabled", havingValue = "true", matchIfMissing = true)
public class MqttActuatorCommandPublisher implements ActuatorCommandPublisher {

    private final MqttCommandPublisher mqttCommandPublisher;
    private final String topicPrefix;

    public MqttActuatorCommandPublisher(MqttCommandPublisher mqttCommandPublisher,
                                        @Value("${smartguard.mqtt.topic-prefix:smartguard}") String topicPrefix) {
        this.mqttCommandPublisher = mqttCommandPublisher;
        this.topicPrefix = topicPrefix;
    }

    @Override
    public void publish(ActuatorCommand command) {
        String topic = topicPrefix + "/devices/" + command.getDevice().getCode() + "/commands";
        mqttCommandPublisher.publish(topic, new MqttActuatorCommandPayload(
                command.getId(),
                command.getActuator().getCode(),
                command.getCommand(),
                command.getPayload()
        ));
    }
}
