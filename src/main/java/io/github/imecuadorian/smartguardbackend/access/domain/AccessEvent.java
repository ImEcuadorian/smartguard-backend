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
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "access_events",
        indexes = {
                @Index(name = "idx_access_events_device_id", columnList = "device_id"),
                @Index(name = "idx_access_events_reader_id", columnList = "reader_id"),
                @Index(name = "idx_access_events_occurred_at", columnList = "occurred_at"),
                @Index(name = "idx_access_events_result", columnList = "result")
        }
)
@EntityListeners(AuditingEntityListener.class)
public class AccessEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "device_id", nullable = false)
    private Device device;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reader_id", nullable = false)
    private AccessReader reader;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "card_id")
    private RfidCard card;

    @Column(name = "card_uid", nullable = false, length = 80)
    private String cardUid;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private AccessResult result;

    @Column(nullable = false, length = 180)
    private String reason;

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected AccessEvent() {
    }

    public AccessEvent(AccessReader reader, RfidCard card, String cardUid, AccessResult result, String reason,
                       Instant occurredAt) {
        this.reader = reader;
        this.device = reader.getDevice();
        this.card = card;
        this.cardUid = cardUid.trim();
        this.result = result;
        this.reason = reason;
        this.occurredAt = occurredAt == null ? Instant.now() : occurredAt;
    }

    public UUID getId() {
        return id;
    }

    public Device getDevice() {
        return device;
    }

    public AccessReader getReader() {
        return reader;
    }

    public RfidCard getCard() {
        return card;
    }

    public String getCardUid() {
        return cardUid;
    }

    public AccessResult getResult() {
        return result;
    }

    public String getReason() {
        return reason;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
