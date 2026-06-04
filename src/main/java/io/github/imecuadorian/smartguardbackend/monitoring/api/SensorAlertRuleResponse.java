package io.github.imecuadorian.smartguardbackend.monitoring.api;

import io.github.imecuadorian.smartguardbackend.alert.domain.AlertSeverity;
import io.github.imecuadorian.smartguardbackend.alert.domain.AlertType;
import io.github.imecuadorian.smartguardbackend.monitoring.domain.ComparisonOperator;
import io.github.imecuadorian.smartguardbackend.monitoring.domain.SensorAlertRuleType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record SensorAlertRuleResponse(
        UUID id,
        UUID sensorId,
        SensorAlertRuleType type,
        ComparisonOperator operator,
        BigDecimal thresholdValue,
        Boolean expectedBooleanValue,
        Integer durationMinutes,
        AlertType alertType,
        AlertSeverity severity,
        String message,
        boolean enabled,
        Instant createdAt,
        Instant updatedAt
) {
}
