package io.github.imecuadorian.smartguardbackend.actuator.api;

import io.github.imecuadorian.smartguardbackend.actuator.domain.Actuator;
import io.github.imecuadorian.smartguardbackend.actuator.domain.ActuatorCommand;
import org.springframework.stereotype.Component;

@Component
public class ActuatorMapper {
    public ActuatorResponse toResponse(Actuator actuator) {
        return new ActuatorResponse(
                actuator.getId(),
                actuator.getDevice().getId(),
                actuator.getCode(),
                actuator.getName(),
                actuator.getType(),
                actuator.getLocation(),
                actuator.getStatus(),
                actuator.getCreatedAt(),
                actuator.getUpdatedAt()
        );
    }

    public ActuatorCommandResponse toResponse(ActuatorCommand command) {
        return new ActuatorCommandResponse(
                command.getId(),
                command.getActuator().getId(),
                command.getDevice().getId(),
                command.getCommand(),
                command.getStatus(),
                command.getPayload(),
                command.getCreatedAt(),
                command.getSentAt()
        );
    }
}
