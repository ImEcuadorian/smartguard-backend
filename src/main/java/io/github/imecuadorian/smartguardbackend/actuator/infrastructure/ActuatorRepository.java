package io.github.imecuadorian.smartguardbackend.actuator.infrastructure;

import io.github.imecuadorian.smartguardbackend.actuator.domain.Actuator;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ActuatorRepository extends JpaRepository<Actuator, UUID> {
    boolean existsByCode(String code);

    Optional<Actuator> findByCode(String code);

    List<Actuator> findAllByOrderByCodeAsc();
}
