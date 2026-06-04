package io.github.imecuadorian.smartguardbackend.alert.domain;

import io.github.imecuadorian.smartguardbackend.device.domain.Device;
import io.github.imecuadorian.smartguardbackend.monitoring.domain.Sensor;
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
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "alerts",
        indexes = {
                @Index(name = "idx_alerts_device_id", columnList = "device_id"),
                @Index(name = "idx_alerts_sensor_id", columnList = "sensor_id"),
                @Index(name = "idx_alerts_status", columnList = "status"),
                @Index(name = "idx_alerts_occurred_at", columnList = "occurred_at")
        }
)
@EntityListeners(AuditingEntityListener.class)
public class Alert {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "device_id")
    private Device device;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sensor_id")
    private Sensor sensor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private AlertType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private AlertSeverity severity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private AlertStatus status;

    @Column(nullable = false, length = 255)
    private String message;

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "acknowledged_at")
    private Instant acknowledgedAt;

    @Column(name = "resolved_at")
    private Instant resolvedAt;

    protected Alert() {
    }

    private Alert(Device device, Sensor sensor, AlertType type, AlertSeverity severity, String message,
                  Instant occurredAt) {
        this.device = device;
        this.sensor = sensor;
        this.type = type;
        this.severity = severity;
        this.message = message.trim();
        this.occurredAt = occurredAt == null ? Instant.now() : occurredAt;
        this.status = AlertStatus.OPEN;
    }

    public static Alert withoutSensor(Device device, AlertType type, AlertSeverity severity, String message,
                                      Instant occurredAt) {
        return new Alert(device, null, type, severity, message, occurredAt);
    }

    public static Alert withSensor(Device device, Sensor sensor, AlertType type, AlertSeverity severity, String message,
                                   Instant occurredAt) {
        return new Alert(device, sensor, type, severity, message, occurredAt);
    }

    public void acknowledge(Instant acknowledgedAt) {
        if (status == AlertStatus.OPEN) {
            status = AlertStatus.ACKNOWLEDGED;
            this.acknowledgedAt = acknowledgedAt == null ? Instant.now() : acknowledgedAt;
        }
    }

    public void resolve(Instant resolvedAt) {
        status = AlertStatus.RESOLVED;
        this.resolvedAt = resolvedAt == null ? Instant.now() : resolvedAt;
    }

    public UUID getId() {
        return id;
    }

    public Device getDevice() {
        return device;
    }

    public Sensor getSensor() {
        return sensor;
    }

    public AlertType getType() {
        return type;
    }

    public AlertSeverity getSeverity() {
        return severity;
    }

    public AlertStatus getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getAcknowledgedAt() {
        return acknowledgedAt;
    }

    public Instant getResolvedAt() {
        return resolvedAt;
    }
}
