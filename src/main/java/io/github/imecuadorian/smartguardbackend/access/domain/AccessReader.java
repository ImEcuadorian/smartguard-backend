package io.github.imecuadorian.smartguardbackend.access.domain;

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
        name = "access_readers",
        uniqueConstraints = @UniqueConstraint(name = "uk_access_readers_code", columnNames = "code"),
        indexes = @Index(name = "idx_access_readers_device_id", columnList = "device_id")
)
@EntityListeners(AuditingEntityListener.class)
public class AccessReader {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "device_id", nullable = false)
    private Device device;

    @Column(nullable = false, unique = true, length = 80)
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private AccessReaderType type;

    @Column(length = 160)
    private String location;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private AccessReaderStatus status;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Version
    private Long version;

    protected AccessReader() {
    }

    public AccessReader(Device device, String code, AccessReaderType type, String location) {
        this.device = device;
        this.code = code.trim();
        this.type = type;
        this.location = normalizeNullable(location);
        this.status = AccessReaderStatus.ACTIVE;
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

    public AccessReaderType getType() {
        return type;
    }

    public String getLocation() {
        return location;
    }

    public AccessReaderStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    private static String normalizeNullable(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
