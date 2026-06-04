package io.github.imecuadorian.smartguardbackend.monitoring.domain;

import io.github.imecuadorian.smartguardbackend.alert.domain.AlertSeverity;
import io.github.imecuadorian.smartguardbackend.alert.domain.AlertType;
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
import jakarta.persistence.Version;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "sensor_alert_rules",
        indexes = {
                @Index(name = "idx_sensor_alert_rules_sensor_id", columnList = "sensor_id"),
                @Index(name = "idx_sensor_alert_rules_type", columnList = "type"),
                @Index(name = "idx_sensor_alert_rules_enabled", columnList = "enabled")
        }
)
@EntityListeners(AuditingEntityListener.class)
public class SensorAlertRule {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sensor_id", nullable = false)
    private Sensor sensor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private SensorAlertRuleType type;

    @Enumerated(EnumType.STRING)
    @Column(length = 40)
    private ComparisonOperator operator;

    @Column(name = "threshold_value", precision = 12, scale = 4)
    private BigDecimal thresholdValue;

    @Column(name = "expected_boolean_value")
    private Boolean expectedBooleanValue;

    @Column(name = "duration_minutes")
    private Integer durationMinutes;

    @Enumerated(EnumType.STRING)
    @Column(name = "alert_type", nullable = false, length = 40)
    private AlertType alertType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private AlertSeverity severity;

    @Column(nullable = false, length = 255)
    private String message;

    @Column(nullable = false)
    private boolean enabled;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Version
    private Long version;

    protected SensorAlertRule() {
    }

    public SensorAlertRule(Sensor sensor, SensorAlertRuleType type, ComparisonOperator operator,
                           BigDecimal thresholdValue, Boolean expectedBooleanValue, Integer durationMinutes,
                           AlertType alertType, AlertSeverity severity, String message) {
        this.sensor = sensor;
        this.type = type;
        this.operator = operator;
        this.thresholdValue = thresholdValue;
        this.expectedBooleanValue = expectedBooleanValue;
        this.durationMinutes = durationMinutes;
        this.alertType = alertType;
        this.severity = severity;
        this.message = normalize(message);
        this.enabled = true;
    }

    public void update(ComparisonOperator operator, BigDecimal thresholdValue, Boolean expectedBooleanValue,
                       Integer durationMinutes, AlertType alertType, AlertSeverity severity, String message,
                       Boolean enabled) {
        if (operator != null) {
            this.operator = operator;
        }
        if (thresholdValue != null) {
            this.thresholdValue = thresholdValue;
        }
        if (expectedBooleanValue != null) {
            this.expectedBooleanValue = expectedBooleanValue;
        }
        if (durationMinutes != null) {
            this.durationMinutes = durationMinutes;
        }
        if (alertType != null) {
            this.alertType = alertType;
        }
        if (severity != null) {
            this.severity = severity;
        }
        if (message != null) {
            this.message = normalize(message);
        }
        if (enabled != null) {
            this.enabled = enabled;
        }
    }

    public void disable() {
        this.enabled = false;
    }

    public UUID getId() {
        return id;
    }

    public Sensor getSensor() {
        return sensor;
    }

    public SensorAlertRuleType getType() {
        return type;
    }

    public ComparisonOperator getOperator() {
        return operator;
    }

    public BigDecimal getThresholdValue() {
        return thresholdValue;
    }

    public Boolean getExpectedBooleanValue() {
        return expectedBooleanValue;
    }

    public Integer getDurationMinutes() {
        return durationMinutes;
    }

    public AlertType getAlertType() {
        return alertType;
    }

    public AlertSeverity getSeverity() {
        return severity;
    }

    public String getMessage() {
        return message;
    }

    public boolean isEnabled() {
        return enabled;
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
