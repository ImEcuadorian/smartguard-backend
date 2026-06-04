package io.github.imecuadorian.smartguardbackend.monitoring.api;

import io.github.imecuadorian.smartguardbackend.monitoring.domain.Sensor;
import io.github.imecuadorian.smartguardbackend.monitoring.domain.SensorReading;
import org.springframework.stereotype.Component;

@Component
public class SensorMapper {
    public SensorResponse toResponse(Sensor sensor) {
        return new SensorResponse(
                sensor.getId(),
                sensor.getDevice().getId(),
                sensor.getCode(),
                sensor.getName(),
                sensor.getType(),
                sensor.getUnit(),
                sensor.getLocation(),
                sensor.getStatus(),
                sensor.getLastReadingAt(),
                sensor.getCreatedAt(),
                sensor.getUpdatedAt()
        );
    }

    public SensorReadingResponse toResponse(SensorReading reading) {
        return new SensorReadingResponse(
                reading.getId(),
                reading.getSensor().getId(),
                reading.getDevice().getId(),
                reading.getNumericValue(),
                reading.getBooleanValue(),
                reading.getTextValue(),
                reading.getRecordedAt(),
                reading.getCreatedAt()
        );
    }
}
