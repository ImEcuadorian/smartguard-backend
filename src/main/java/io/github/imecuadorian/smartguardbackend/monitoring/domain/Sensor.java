package io.github.imecuadorian.smartguardbackend.monitoring.domain;

import io.github.imecuadorian.smartguardbackend.device.domain.Device;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "sensors",
        uniqueConstraints = @UniqueConstraint(name = "uk_sensors_code", columnNames = "code"),
        indexes = {
                @Index(name = "idx_sensors_device_id", columnList = "device_id"),
                @Index(name = "idx_sensors_type", columnList = "type"),
                @Index(name = "idx_sensors_status", columnList = "status"),
                @Index(name = "idx_sensors_last_reading_at", columnList = "last_reading_at")
        }
)
@EntityListeners(AuditingEntityListener.class)
public class Sensor {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "device_id", nullable = false)
    private Device device;

    @Column(nullable = false, unique = true, length = 80)
    private String code;

    @Column(nullable = false, length = 120)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private SensorType type;

    @Column(length = 40)
    private String unit;

    @Column(length = 160)
    private String location;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private SensorStatus status;

    @Column(name = "last_reading_at")
    private Instant lastReadingAt;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Version
    private Long version;

    protected Sensor() {
    }

    public Sensor(Device device, String code, String name, SensorType type, String unit, String location) {
        this.device = device;
        this.code = normalize(code);
        this.name = normalize(name);
        this.type = type;
        this.unit = normalizeNullable(unit);
        this.location = normalizeNullable(location);
        this.status = SensorStatus.ACTIVE;
    }

    public void updateStatus(SensorStatus status) {
        this.status = status;
    }

    public void markReadingAt(Instant readingAt) {
        this.lastReadingAt = readingAt == null ? Instant.now() : readingAt;
        if (status == SensorStatus.INACTIVE) {
            this.status = SensorStatus.ACTIVE;
        }
    }

    public UUID getId() {
        return id;
    }

    public Device getDevice() {
        return device;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public SensorType getType() {
        return type;
    }

    public String getUnit() {
        return unit;
    }

    public String getLocation() {
        return location;
    }

    public SensorStatus getStatus() {
        return status;
    }

    public Instant getLastReadingAt() {
        return lastReadingAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    private static String normalize(String value) {
        return value.trim();
    }

    private static String normalizeNullable(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
