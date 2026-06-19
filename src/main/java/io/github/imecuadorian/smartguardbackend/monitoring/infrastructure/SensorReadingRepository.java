package io.github.imecuadorian.smartguardbackend.monitoring.infrastructure;

import io.github.imecuadorian.smartguardbackend.monitoring.domain.SensorReading;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SensorReadingRepository extends JpaRepository<SensorReading, UUID> {

    List<SensorReading> findAllBySensorIdOrderByRecordedAtDesc(UUID sensorId);

    Optional<SensorReading> findFirstBySensorIdOrderByRecordedAtDesc(UUID sensorId);

    List<SensorReading> findBySensorIdOrderByRecordedAtDesc(
            UUID sensorId,
            Pageable pageable
    );

    List<SensorReading> findBySensorIdAndRecordedAtGreaterThanEqualOrderByRecordedAtDesc(
            UUID sensorId,
            Instant from,
            Pageable pageable
    );

    List<SensorReading> findBySensorIdAndRecordedAtLessThanEqualOrderByRecordedAtDesc(
            UUID sensorId,
            Instant to,
            Pageable pageable
    );

    List<SensorReading> findBySensorIdAndRecordedAtBetweenOrderByRecordedAtDesc(
            UUID sensorId,
            Instant from,
            Instant to,
            Pageable pageable
    );

    boolean existsBySensorIdAndBooleanValueAndRecordedAtLessThanEqual(
            UUID sensorId,
            Boolean booleanValue,
            Instant recordedAt
    );

    boolean existsBySensorIdAndBooleanValueAndRecordedAtGreaterThan(
            UUID sensorId,
            Boolean booleanValue,
            Instant recordedAt
    );
}