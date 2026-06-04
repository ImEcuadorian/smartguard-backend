package io.github.imecuadorian.smartguardbackend.monitoring.infrastructure;

import io.github.imecuadorian.smartguardbackend.monitoring.domain.Sensor;
import io.github.imecuadorian.smartguardbackend.monitoring.domain.SensorStatus;
import io.github.imecuadorian.smartguardbackend.monitoring.domain.SensorType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SensorRepository extends JpaRepository<Sensor, UUID> {
    boolean existsByCode(String code);

    Optional<Sensor> findByCode(String code);

    List<Sensor> findAllByOrderByCodeAsc();

    @Query("""
            select sensor
            from Sensor sensor
            where (:deviceId is null or sensor.device.id = :deviceId)
              and (:status is null or sensor.status = :status)
              and (:type is null or sensor.type = :type)
            order by sensor.code asc
            """)
    List<Sensor> findAllFiltered(@Param("deviceId") UUID deviceId, @Param("status") SensorStatus status,
                                 @Param("type") SensorType type);
}
