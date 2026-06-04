package io.github.imecuadorian.smartguardbackend.access.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
        name = "rfid_cards",
        uniqueConstraints = @UniqueConstraint(name = "uk_rfid_cards_uid", columnNames = "uid")
)
@EntityListeners(AuditingEntityListener.class)
public class RfidCard {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, length = 80)
    private String uid;

    @Column(name = "owner_name", nullable = false, length = 120)
    private String ownerName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private RfidCardStatus status;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Version
    private Long version;

    protected RfidCard() {
    }

    public RfidCard(String uid, String ownerName) {
        this.uid = normalize(uid);
        this.ownerName = normalize(ownerName);
        this.status = RfidCardStatus.ACTIVE;
    }

    public boolean isActive() {
        return status == RfidCardStatus.ACTIVE;
    }

    public UUID getId() {
        return id;
    }

    public String getUid() {
        return uid;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public RfidCardStatus getStatus() {
        return status;
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
}
