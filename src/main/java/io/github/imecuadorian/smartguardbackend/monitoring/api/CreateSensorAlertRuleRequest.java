package io.github.imecuadorian.smartguardbackend.monitoring.api;

import io.github.imecuadorian.smartguardbackend.alert.domain.AlertSeverity;
import io.github.imecuadorian.smartguardbackend.alert.domain.AlertType;
import io.github.imecuadorian.smartguardbackend.monitoring.domain.ComparisonOperator;
import io.github.imecuadorian.smartguardbackend.monitoring.domain.SensorAlertRuleType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record CreateSensorAlertRuleRequest(
        @NotNull(message = "Rule type is required")
        SensorAlertRuleType type,

        ComparisonOperator operator,

        BigDecimal thresholdValue,

        Boolean expectedBooleanValue,

        @Min(value = 1, message = "Duration minutes must be at least 1")
        Integer durationMinutes,

        @NotNull(message = "Alert type is required")
        AlertType alertType,

        @NotNull(message = "Alert severity is required")
        AlertSeverity severity,

        @NotBlank(message = "Alert message is required")
        @Size(max = 255, message = "Alert message must have at most 255 characters")
        String message
) {
}
