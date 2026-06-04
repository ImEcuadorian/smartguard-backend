package io.github.imecuadorian.smartguardbackend.actuator.infrastructure;

import io.github.imecuadorian.smartguardbackend.actuator.domain.ActuatorCommand;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ActuatorCommandRepository extends JpaRepository<ActuatorCommand, UUID> {
    List<ActuatorCommand> findAllByActuatorIdOrderByCreatedAtDesc(UUID actuatorId);
}
