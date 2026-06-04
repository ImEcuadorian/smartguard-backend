package io.github.imecuadorian.smartguardbackend.device.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
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
        name = "devices",
        uniqueConstraints = @UniqueConstraint(name = "uk_devices_code", columnNames = "code"),
        indexes = {
                @Index(name = "idx_devices_status", columnList = "status"),
                @Index(name = "idx_devices_code", columnList = "code")
        }
)
@EntityListeners(AuditingEntityListener.class)
public class Device {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, length = 80)
    private String code;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(length = 160)
    private String location;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private DeviceStatus status;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "firmware_version", length = 40)
    private String firmwareVersion;

    @Column(name = "api_key_hash", length = 120)
    private String apiKeyHash;

    @Column(name = "last_seen_at")
    private Instant lastSeenAt;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Version
    private Long version;

    protected Device() {
    }

    public Device(String code, String name, String location, String ipAddress, String firmwareVersion) {
        this.code = normalize(code);
        this.name = normalize(name);
        this.location = normalizeNullable(location);
        this.ipAddress = normalizeNullable(ipAddress);
        this.firmwareVersion = normalizeNullable(firmwareVersion);
        this.status = DeviceStatus.ACTIVE;
    }

    public void updateDetails(String code, String name, String location, String ipAddress, String firmwareVersion) {
        if (code != null) {
            this.code = normalize(code);
        }
        if (name != null) {
            this.name = normalize(name);
        }
        if (location != null) {
            this.location = normalizeNullable(location);
        }
        if (ipAddress != null) {
            this.ipAddress = normalizeNullable(ipAddress);
        }
        if (firmwareVersion != null) {
            this.firmwareVersion = normalizeNullable(firmwareVersion);
        }
    }

    public void updateStatus(DeviceStatus status) {
        this.status = status;
    }

    public void updateApiKeyHash(String apiKeyHash) {
        this.apiKeyHash = normalize(apiKeyHash);
    }

    public void markSeen(Instant seenAt) {
        this.lastSeenAt = seenAt;
    }

    public UUID getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getLocation() {
        return location;
    }

    public DeviceStatus getStatus() {
        return status;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public String getFirmwareVersion() {
        return firmwareVersion;
    }

    public String getApiKeyHash() {
        return apiKeyHash;
    }

    public Instant getLastSeenAt() {
        return lastSeenAt;
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
