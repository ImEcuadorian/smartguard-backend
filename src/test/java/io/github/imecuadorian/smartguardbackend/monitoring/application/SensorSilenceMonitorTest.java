package io.github.imecuadorian.smartguardbackend.monitoring.application;

import io.github.imecuadorian.smartguardbackend.alert.application.AlertService;
import io.github.imecuadorian.smartguardbackend.alert.domain.AlertSeverity;
import io.github.imecuadorian.smartguardbackend.alert.domain.AlertType;
import io.github.imecuadorian.smartguardbackend.device.domain.Device;
import io.github.imecuadorian.smartguardbackend.monitoring.domain.Sensor;
import io.github.imecuadorian.smartguardbackend.monitoring.domain.SensorAlertRule;
import io.github.imecuadorian.smartguardbackend.monitoring.domain.SensorAlertRuleType;
import io.github.imecuadorian.smartguardbackend.monitoring.domain.SensorStatus;
import io.github.imecuadorian.smartguardbackend.monitoring.domain.SensorType;
import io.github.imecuadorian.smartguardbackend.monitoring.infrastructure.SensorAlertRuleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SensorSilenceMonitorTest {

    @Mock
    private SensorAlertRuleRepository ruleRepository;

    @Mock
    private AlertService alertService;

    @Test
    void createsAlertAndMarksSensorInactiveWhenNoReadingArrivesWithinConfiguredMinutes() {
        var device = new Device("esp32-001", "ESP32 Main Door", null, null, null);
        var sensor = new Sensor(device, "gas-main", "Main gas sensor", SensorType.GAS, "ppm", null);
        sensor.markReadingAt(Instant.parse("2026-06-04T01:00:00Z"));
        var rule = new SensorAlertRule(
                sensor,
                SensorAlertRuleType.NO_READING,
                null,
                null,
                null,
                5,
                AlertType.DEVICE_OFFLINE,
                AlertSeverity.WARNING,
                "Sensor gas-main stopped reporting"
        );

        when(ruleRepository.findAllByTypeAndEnabledTrue(SensorAlertRuleType.NO_READING)).thenReturn(List.of(rule));

        var monitor = new SensorSilenceMonitor(
                ruleRepository,
                alertService,
                Clock.fixed(Instant.parse("2026-06-04T01:10:00Z"), ZoneOffset.UTC)
        );
        monitor.checkSilentSensors();

        assertThat(sensor.getStatus()).isEqualTo(SensorStatus.INACTIVE);
        verify(alertService).createAutomaticAlertIfAbsent(
                device,
                sensor,
                AlertType.DEVICE_OFFLINE,
                AlertSeverity.WARNING,
                "Sensor gas-main stopped reporting",
                Instant.parse("2026-06-04T01:10:00Z")
        );
    }
}
