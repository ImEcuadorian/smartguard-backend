package io.github.imecuadorian.smartguardbackend.actuator.application;

import io.github.imecuadorian.smartguardbackend.actuator.domain.ActuatorCommand;

public interface ActuatorCommandPublisher {
    void publish(ActuatorCommand command);

    static ActuatorCommandPublisher noop() {
        return command -> {
        };
    }
}
