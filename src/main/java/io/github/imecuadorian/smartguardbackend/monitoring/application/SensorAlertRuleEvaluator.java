package io.github.imecuadorian.smartguardbackend.monitoring.application;

import io.github.imecuadorian.smartguardbackend.alert.application.AlertService;
import io.github.imecuadorian.smartguardbackend.monitoring.domain.SensorAlertRule;
import io.github.imecuadorian.smartguardbackend.monitoring.domain.SensorAlertRuleType;
import io.github.imecuadorian.smartguardbackend.monitoring.domain.SensorReading;
import io.github.imecuadorian.smartguardbackend.monitoring.infrastructure.SensorAlertRuleRepository;
import io.github.imecuadorian.smartguardbackend.monitoring.infrastructure.SensorReadingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Objects;

@Component
public class SensorAlertRuleEvaluator {

    private final SensorAlertRuleRepository ruleRepository;
    private final SensorReadingRepository readingRepository;
    private final AlertService alertService;

    public SensorAlertRuleEvaluator() {
        this(null, null, null);
    }

    @Autowired
    public SensorAlertRuleEvaluator(SensorAlertRuleRepository ruleRepository, SensorReadingRepository readingRepository,
                                    AlertService alertService) {
        this.ruleRepository = ruleRepository;
        this.readingRepository = readingRepository;
        this.alertService = alertService;
    }

    public static SensorAlertRuleEvaluator noop() {
        return new SensorAlertRuleEvaluator();
    }

    public void evaluate(SensorReading reading) {
        if (ruleRepository == null || alertService == null) {
            return;
        }

        var rules = ruleRepository.findAllBySensorIdAndEnabledTrue(reading.getSensor().getId());
        for (SensorAlertRule rule : rules) {
            if (isTriggered(rule, reading)) {
                alertService.createAutomaticAlertIfAbsent(
                        reading.getDevice(),
                        reading.getSensor(),
                        rule.getAlertType(),
                        rule.getSeverity(),
                        rule.getMessage(),
                        reading.getRecordedAt()
                );
            }
        }
    }

    private boolean isTriggered(SensorAlertRule rule, SensorReading reading) {
        return switch (rule.getType()) {
            case NUMERIC_THRESHOLD -> isNumericThresholdTriggered(rule, reading);
            case BOOLEAN_MATCH -> Objects.equals(reading.getBooleanValue(), rule.getExpectedBooleanValue());
            case DURATION_OPEN -> isDurationOpenTriggered(rule, reading);
            case NO_READING -> false;
        };
    }

    private boolean isNumericThresholdTriggered(SensorAlertRule rule, SensorReading reading) {
        return reading.getNumericValue() != null
                && rule.getOperator() != null
                && rule.getThresholdValue() != null
                && rule.getOperator().matches(reading.getNumericValue(), rule.getThresholdValue());
    }

    private boolean isDurationOpenTriggered(SensorAlertRule rule, SensorReading reading) {
        if (readingRepository == null
                || rule.getExpectedBooleanValue() == null
                || rule.getDurationMinutes() == null
                || !Objects.equals(reading.getBooleanValue(), rule.getExpectedBooleanValue())) {
            return false;
        }

        var cutoff = reading.getRecordedAt().minus(Duration.ofMinutes(rule.getDurationMinutes()));
        boolean stateWasAlreadyOpen = readingRepository.existsBySensorIdAndBooleanValueAndRecordedAtLessThanEqual(
                reading.getSensor().getId(),
                rule.getExpectedBooleanValue(),
                cutoff
        );
        boolean stateClosedRecently = readingRepository.existsBySensorIdAndBooleanValueAndRecordedAtGreaterThan(
                reading.getSensor().getId(),
                !rule.getExpectedBooleanValue(),
                cutoff
        );
        return stateWasAlreadyOpen && !stateClosedRecently;
    }
}
