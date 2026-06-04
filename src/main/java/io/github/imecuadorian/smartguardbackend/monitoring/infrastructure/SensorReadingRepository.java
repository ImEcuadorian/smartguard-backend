package io.github.imecuadorian.smartguardbackend.monitoring.infrastructure;

import io.github.imecuadorian.smartguardbackend.monitoring.domain.SensorReading;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SensorReadingRepository extends JpaRepository<SensorReading, UUID> {
    List<SensorReading> findAllBySensorIdOrderByRecordedAtDesc(UUID sensorId);

    Optional<SensorReading> findFirstBySensorIdOrderByRecordedAtDesc(UUID sensorId);

    @Query("""
            select reading
            from SensorReading reading
            where reading.sensor.id = :sensorId
              and (:from is null or reading.recordedAt >= :from)
              and (:to is null or reading.recordedAt <= :to)
            order by reading.recordedAt desc
            """)
    List<SensorReading> findReadings(@Param("sensorId") UUID sensorId, @Param("from") Instant from,
                                     @Param("to") Instant to, Pageable pageable);

    boolean existsBySensorIdAndBooleanValueAndRecordedAtLessThanEqual(UUID sensorId, Boolean booleanValue,
                                                                      Instant recordedAt);

    boolean existsBySensorIdAndBooleanValueAndRecordedAtGreaterThan(UUID sensorId, Boolean booleanValue,
                                                                    Instant recordedAt);
}
