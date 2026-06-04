package io.github.imecuadorian.smartguardbackend.monitoring.domain;

import io.github.imecuadorian.smartguardbackend.device.domain.Device;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "sensor_readings",
        indexes = {
                @Index(name = "idx_sensor_readings_sensor_id", columnList = "sensor_id"),
                @Index(name = "idx_sensor_readings_device_id", columnList = "device_id"),
                @Index(name = "idx_sensor_readings_recorded_at", columnList = "recorded_at")
        }
)
@EntityListeners(AuditingEntityListener.class)
public class SensorReading {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sensor_id", nullable = false)
    private Sensor sensor;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "device_id", nullable = false)
    private Device device;

    @Column(name = "numeric_value", precision = 12, scale = 4)
    private BigDecimal numericValue;

    @Column(name = "boolean_value")
    private Boolean booleanValue;

    @Column(name = "text_value", length = 255)
    private String textValue;

    @Column(name = "recorded_at", nullable = false)
    private Instant recordedAt;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected SensorReading() {
    }

    public SensorReading(Sensor sensor, BigDecimal numericValue, Boolean booleanValue, String textValue,
                         Instant recordedAt) {
        this.sensor = sensor;
        this.device = sensor.getDevice();
        this.numericValue = numericValue;
        this.booleanValue = booleanValue;
        this.textValue = normalizeNullable(textValue);
        this.recordedAt = recordedAt == null ? Instant.now() : recordedAt;
    }

    public UUID getId() {
        return id;
    }

    public Sensor getSensor() {
        return sensor;
    }

    public Device getDevice() {
        return device;
    }

    public BigDecimal getNumericValue() {
        return numericValue;
    }

    public Boolean getBooleanValue() {
        return booleanValue;
    }

    public String getTextValue() {
        return textValue;
    }

    public Instant getRecordedAt() {
        return recordedAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    private static String normalizeNullable(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
