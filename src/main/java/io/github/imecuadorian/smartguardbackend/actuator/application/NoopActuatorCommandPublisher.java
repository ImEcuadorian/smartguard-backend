package io.github.imecuadorian.smartguardbackend.actuator.application;

import io.github.imecuadorian.smartguardbackend.actuator.domain.ActuatorCommand;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "smartguard.mqtt.enabled", havingValue = "false")
public class NoopActuatorCommandPublisher implements ActuatorCommandPublisher {
    @Override
    public void publish(ActuatorCommand command) {
    }
}
