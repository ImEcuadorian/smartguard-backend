package io.github.imecuadorian.smartguardbackend.alert.infrastructure;

import io.github.imecuadorian.smartguardbackend.alert.domain.Alert;
import io.github.imecuadorian.smartguardbackend.alert.domain.AlertSeverity;
import io.github.imecuadorian.smartguardbackend.alert.domain.AlertStatus;
import io.github.imecuadorian.smartguardbackend.alert.domain.AlertType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface AlertRepository extends JpaRepository<Alert, UUID> {
    List<Alert> findAllByOrderByOccurredAtDesc();

    @Query("""
            select alert
            from Alert alert
            where (:status is null or alert.status = :status)
              and (:severity is null or alert.severity = :severity)
            order by alert.occurredAt desc
            """)
    List<Alert> findAllFiltered(@Param("status") AlertStatus status, @Param("severity") AlertSeverity severity);

    boolean existsBySensorIdAndTypeAndStatusIn(UUID sensorId, AlertType type, Collection<AlertStatus> statuses);
}
