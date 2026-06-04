package io.github.imecuadorian.smartguardbackend.monitoring.api;

import io.github.imecuadorian.smartguardbackend.alert.domain.AlertSeverity;
import io.github.imecuadorian.smartguardbackend.alert.domain.AlertType;
import io.github.imecuadorian.smartguardbackend.monitoring.domain.ComparisonOperator;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record UpdateSensorAlertRuleRequest(
        ComparisonOperator operator,
        BigDecimal thresholdValue,
        Boolean expectedBooleanValue,

        @Min(value = 1, message = "Duration minutes must be at least 1")
        Integer durationMinutes,

        AlertType alertType,
        AlertSeverity severity,

        @Size(max = 255, message = "Alert message must have at most 255 characters")
        String message,

        Boolean enabled
) {
}
