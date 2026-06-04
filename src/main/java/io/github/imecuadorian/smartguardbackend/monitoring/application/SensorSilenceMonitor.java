package io.github.imecuadorian.smartguardbackend.monitoring.application;

import io.github.imecuadorian.smartguardbackend.alert.application.AlertService;
import io.github.imecuadorian.smartguardbackend.monitoring.domain.SensorAlertRule;
import io.github.imecuadorian.smartguardbackend.monitoring.domain.SensorAlertRuleType;
import io.github.imecuadorian.smartguardbackend.monitoring.domain.SensorStatus;
import io.github.imecuadorian.smartguardbackend.monitoring.infrastructure.SensorAlertRuleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

@Component
@ConditionalOnProperty(name = "smartguard.monitoring.silence-monitor.enabled", havingValue = "true",
        matchIfMissing = true)
public class SensorSilenceMonitor {

    private final SensorAlertRuleRepository ruleRepository;
    private final AlertService alertService;
    private final Clock clock;

    @Autowired
    public SensorSilenceMonitor(SensorAlertRuleRepository ruleRepository, AlertService alertService) {
        this(ruleRepository, alertService, Clock.systemUTC());
    }

    public SensorSilenceMonitor(SensorAlertRuleRepository ruleRepository, AlertService alertService, Clock clock) {
        this.ruleRepository = ruleRepository;
        this.alertService = alertService;
        this.clock = clock;
    }

    @Scheduled(fixedDelayString = "${smartguard.monitoring.silence-monitor.fixed-delay-ms:60000}")
    @Transactional
    public void checkSilentSensors() {
        Instant now = Instant.now(clock);
        for (SensorAlertRule rule : ruleRepository.findAllByTypeAndEnabledTrue(SensorAlertRuleType.NO_READING)) {
            if (isSilent(rule, now)) {
                var sensor = rule.getSensor();
                if (sensor.getStatus() != SensorStatus.MAINTENANCE) {
                    sensor.updateStatus(SensorStatus.INACTIVE);
                }
                alertService.createAutomaticAlertIfAbsent(
                        sensor.getDevice(),
                        sensor,
                        rule.getAlertType(),
                        rule.getSeverity(),
                        rule.getMessage(),
                        now
                );
            }
        }
    }

    private boolean isSilent(SensorAlertRule rule, Instant now) {
        if (rule.getDurationMinutes() == null || rule.getDurationMinutes() < 1) {
            return false;
        }

        Instant lastReadingAt = rule.getSensor().getLastReadingAt();
        Instant cutoff = now.minus(Duration.ofMinutes(rule.getDurationMinutes()));
        return lastReadingAt == null || lastReadingAt.isBefore(cutoff) || lastReadingAt.equals(cutoff);
    }
}
