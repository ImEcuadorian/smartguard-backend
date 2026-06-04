package io.github.imecuadorian.smartguardbackend.monitoring.infrastructure;

import io.github.imecuadorian.smartguardbackend.monitoring.domain.SensorAlertRule;
import io.github.imecuadorian.smartguardbackend.monitoring.domain.SensorAlertRuleType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SensorAlertRuleRepository extends JpaRepository<SensorAlertRule, UUID> {
    List<SensorAlertRule> findAllBySensorIdOrderByCreatedAtAsc(UUID sensorId);

    List<SensorAlertRule> findAllBySensorIdAndEnabledTrue(UUID sensorId);

    List<SensorAlertRule> findAllByTypeAndEnabledTrue(SensorAlertRuleType type);
}
