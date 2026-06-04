package io.github.imecuadorian.smartguardbackend.monitoring.api;

import io.github.imecuadorian.smartguardbackend.monitoring.domain.SensorAlertRule;
import org.springframework.stereotype.Component;

@Component
public class SensorAlertRuleMapper {
    public SensorAlertRuleResponse toResponse(SensorAlertRule rule) {
        return new SensorAlertRuleResponse(
                rule.getId(),
                rule.getSensor().getId(),
                rule.getType(),
                rule.getOperator(),
                rule.getThresholdValue(),
                rule.getExpectedBooleanValue(),
                rule.getDurationMinutes(),
                rule.getAlertType(),
                rule.getSeverity(),
                rule.getMessage(),
                rule.isEnabled(),
                rule.getCreatedAt(),
                rule.getUpdatedAt()
        );
    }
}
