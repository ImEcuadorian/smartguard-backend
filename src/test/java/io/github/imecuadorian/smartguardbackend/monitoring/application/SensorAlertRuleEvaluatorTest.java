package io.github.imecuadorian.smartguardbackend.monitoring.application;

import io.github.imecuadorian.smartguardbackend.alert.application.AlertService;
import io.github.imecuadorian.smartguardbackend.alert.domain.AlertSeverity;
import io.github.imecuadorian.smartguardbackend.alert.domain.AlertType;
import io.github.imecuadorian.smartguardbackend.device.domain.Device;
import io.github.imecuadorian.smartguardbackend.monitoring.domain.ComparisonOperator;
import io.github.imecuadorian.smartguardbackend.monitoring.domain.Sensor;
import io.github.imecuadorian.smartguardbackend.monitoring.domain.SensorAlertRule;
import io.github.imecuadorian.smartguardbackend.monitoring.domain.SensorAlertRuleType;
import io.github.imecuadorian.smartguardbackend.monitoring.domain.SensorReading;
import io.github.imecuadorian.smartguardbackend.monitoring.domain.SensorType;
import io.github.imecuadorian.smartguardbackend.monitoring.infrastructure.SensorAlertRuleRepository;
import io.github.imecuadorian.smartguardbackend.monitoring.infrastructure.SensorReadingRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SensorAlertRuleEvaluatorTest {

    @Mock
    private SensorAlertRuleRepository ruleRepository;

    @Mock
    private SensorReadingRepository readingRepository;

    @Mock
    private AlertService alertService;

    @Test
    void createsAutomaticAlertWhenNumericReadingExceedsThreshold() {
        var device = new Device("esp32-001", "ESP32 Main Door", null, null, null);
        var sensor = new Sensor(device, "gas-main", "Main gas sensor", SensorType.GAS, "ppm", null);
        var reading = new SensorReading(sensor, new BigDecimal("750"), null, null,
                Instant.parse("2026-06-04T01:00:00Z"));
        var rule = new SensorAlertRule(
                sensor,
                SensorAlertRuleType.NUMERIC_THRESHOLD,
                ComparisonOperator.GREATER_THAN,
                new BigDecimal("700"),
                null,
                null,
                AlertType.GAS_DETECTED,
                AlertSeverity.CRITICAL,
                "Gas level exceeded 700 ppm"
        );

        when(ruleRepository.findAllBySensorIdAndEnabledTrue(sensor.getId())).thenReturn(List.of(rule));

        var evaluator = new SensorAlertRuleEvaluator(ruleRepository, readingRepository, alertService);
        evaluator.evaluate(reading);

        verify(alertService).createAutomaticAlertIfAbsent(
                device,
                sensor,
                AlertType.GAS_DETECTED,
                AlertSeverity.CRITICAL,
                "Gas level exceeded 700 ppm",
                reading.getRecordedAt()
        );
    }
}
