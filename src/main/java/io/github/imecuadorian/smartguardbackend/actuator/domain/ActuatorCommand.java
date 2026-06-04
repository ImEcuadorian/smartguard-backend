package io.github.imecuadorian.smartguardbackend.actuator.domain;

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
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "actuator_commands",
        indexes = {
                @Index(name = "idx_actuator_commands_actuator_id", columnList = "actuator_id"),
                @Index(name = "idx_actuator_commands_device_id", columnList = "device_id"),
                @Index(name = "idx_actuator_commands_status", columnList = "status")
        }
)
@EntityListeners(AuditingEntityListener.class)
public class ActuatorCommand {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "actuator_id", nullable = false)
    private Actuator actuator;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "device_id", nullable = false)
    private Device device;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private ActuatorCommandType command;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ActuatorCommandStatus status;

    @Column(length = 255)
    private String payload;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "sent_at")
    private Instant sentAt;

    protected ActuatorCommand() {
    }

    public ActuatorCommand(Actuator actuator, ActuatorCommandType command, String payload) {
        this.actuator = actuator;
        this.device = actuator.getDevice();
        this.command = command;
        this.payload = normalizeNullable(payload);
        this.status = ActuatorCommandStatus.PENDING;
    }

    public void markSent(Instant sentAt) {
        this.status = ActuatorCommandStatus.SENT;
        this.sentAt = sentAt == null ? Instant.now() : sentAt;
    }

    public UUID getId() {
        return id;
    }

    public Actuator getActuator() {
        return actuator;
    }

    public Device getDevice() {
        return device;
    }

    public ActuatorCommandType getCommand() {
        return command;
    }

    public ActuatorCommandStatus getStatus() {
        return status;
    }

    public String getPayload() {
        return payload;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getSentAt() {
        return sentAt;
    }

    private static String normalizeNullable(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
