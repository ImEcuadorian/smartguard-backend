package io.github.imecuadorian.smartguardbackend.monitoring.application;

import io.github.imecuadorian.smartguardbackend.alert.domain.AlertSeverity;
import io.github.imecuadorian.smartguardbackend.alert.domain.AlertType;
import io.github.imecuadorian.smartguardbackend.device.domain.Device;
import io.github.imecuadorian.smartguardbackend.monitoring.api.CreateSensorAlertRuleRequest;
import io.github.imecuadorian.smartguardbackend.monitoring.api.SensorAlertRuleMapper;
import io.github.imecuadorian.smartguardbackend.monitoring.domain.ComparisonOperator;
import io.github.imecuadorian.smartguardbackend.monitoring.domain.Sensor;
import io.github.imecuadorian.smartguardbackend.monitoring.domain.SensorAlertRule;
import io.github.imecuadorian.smartguardbackend.monitoring.domain.SensorAlertRuleType;
import io.github.imecuadorian.smartguardbackend.monitoring.domain.SensorType;
import io.github.imecuadorian.smartguardbackend.monitoring.infrastructure.SensorAlertRuleRepository;
import io.github.imecuadorian.smartguardbackend.monitoring.infrastructure.SensorRepository;
import io.github.imecuadorian.smartguardbackend.shared.error.InvalidRequestException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SensorAlertRuleServiceTest {

    private static final UUID SENSOR_ID = UUID.fromString("aabdb5ce-9531-42e6-a43e-4368cc9dca9e");

    @Mock
    private SensorRepository sensorRepository;

    @Mock
    private SensorAlertRuleRepository ruleRepository;

    private SensorAlertRuleService ruleService;

    @BeforeEach
    void setUp() {
        ruleService = new SensorAlertRuleService(sensorRepository, ruleRepository, new SensorAlertRuleMapper());
    }

    @Test
    void createNumericThresholdRuleStoresEnabledRule() {
        var sensor = new Sensor(new Device("esp32-001", "ESP32 Main Door", null, null, null),
                "gas-main", "Main gas sensor", SensorType.GAS, "ppm", null);
        var request = new CreateSensorAlertRuleRequest(
                SensorAlertRuleType.NUMERIC_THRESHOLD,
                ComparisonOperator.GREATER_THAN,
                new BigDecimal("700"),
                null,
                null,
                AlertType.GAS_DETECTED,
                AlertSeverity.CRITICAL,
                "Gas level exceeded 700 ppm"
        );

        when(sensorRepository.findById(SENSOR_ID)).thenReturn(Optional.of(sensor));
        when(ruleRepository.save(any(SensorAlertRule.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = ruleService.create(SENSOR_ID, request);

        assertThat(response.type()).isEqualTo(SensorAlertRuleType.NUMERIC_THRESHOLD);
        assertThat(response.thresholdValue()).isEqualByComparingTo("700");
        assertThat(response.enabled()).isTrue();
    }

    @Test
    void createNumericThresholdRuleRejectsMissingOperator() {
        var sensor = new Sensor(new Device("esp32-001", "ESP32 Main Door", null, null, null),
                "gas-main", "Main gas sensor", SensorType.GAS, "ppm", null);
        var request = new CreateSensorAlertRuleRequest(
                SensorAlertRuleType.NUMERIC_THRESHOLD,
                null,
                new BigDecimal("700"),
                null,
                null,
                AlertType.GAS_DETECTED,
                AlertSeverity.CRITICAL,
                "Gas level exceeded 700 ppm"
        );

        when(sensorRepository.findById(SENSOR_ID)).thenReturn(Optional.of(sensor));

        assertThatThrownBy(() -> ruleService.create(SENSOR_ID, request))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessage("Numeric threshold rules require operator and thresholdValue");
    }
}
