package io.github.imecuadorian.smartguardbackend.alert.application;

import io.github.imecuadorian.smartguardbackend.alert.api.AlertMapper;
import io.github.imecuadorian.smartguardbackend.alert.api.CreateAlertRequest;
import io.github.imecuadorian.smartguardbackend.alert.domain.Alert;
import io.github.imecuadorian.smartguardbackend.alert.domain.AlertSeverity;
import io.github.imecuadorian.smartguardbackend.alert.domain.AlertStatus;
import io.github.imecuadorian.smartguardbackend.alert.domain.AlertType;
import io.github.imecuadorian.smartguardbackend.alert.infrastructure.AlertRepository;
import io.github.imecuadorian.smartguardbackend.device.domain.Device;
import io.github.imecuadorian.smartguardbackend.device.infrastructure.DeviceRepository;
import io.github.imecuadorian.smartguardbackend.monitoring.domain.Sensor;
import io.github.imecuadorian.smartguardbackend.monitoring.domain.SensorType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AlertServiceTest {

    private static final UUID DEVICE_ID = UUID.fromString("f6f928d2-1d8c-4ab1-9b8d-ec1a3d2f4f64");
    private static final UUID SENSOR_ID = UUID.fromString("aabdb5ce-9531-42e6-a43e-4368cc9dca9e");
    private static final UUID ALERT_ID = UUID.fromString("46539ac7-a7cc-4212-bd06-16817b431917");

    @Mock
    private DeviceRepository deviceRepository;

    @Mock
    private AlertRepository alertRepository;

    private AlertService alertService;

    @BeforeEach
    void setUp() {
        alertService = new AlertService(deviceRepository, null, alertRepository, new AlertMapper());
    }

    @Test
    void createAlertStoresOpenAlert() {
        var device = new Device("esp32-001", "ESP32 Main Door", null, null, null);

        when(deviceRepository.findById(DEVICE_ID)).thenReturn(Optional.of(device));
        when(alertRepository.save(any(Alert.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = alertService.create(new CreateAlertRequest(
                DEVICE_ID,
                null,
                AlertType.GAS_DETECTED,
                AlertSeverity.CRITICAL,
                "Gas level exceeded threshold",
                Instant.parse("2026-06-03T20:00:00Z")
        ));

        assertThat(response.status()).isEqualTo(AlertStatus.OPEN);
        assertThat(response.severity()).isEqualTo(AlertSeverity.CRITICAL);
    }

    @Test
    void acknowledgeMovesAlertToAcknowledged() {
        var alert = Alert.withoutSensor(null, AlertType.ACCESS_DENIED, AlertSeverity.WARNING,
                "Unknown card", Instant.parse("2026-06-03T20:00:00Z"));

        when(alertRepository.findById(ALERT_ID)).thenReturn(Optional.of(alert));

        var response = alertService.acknowledge(ALERT_ID);

        assertThat(response.status()).isEqualTo(AlertStatus.ACKNOWLEDGED);
    }

    @Test
    void automaticAlertIsNotCreatedWhenSameSensorAlreadyHasAnOpenOrAcknowledgedAlert() {
        var device = new Device("esp32-001", "ESP32 Main Door", null, null, null);
        var sensor = new Sensor(device, "gas-main", "Main gas sensor", SensorType.GAS, "ppm", null);
        ReflectionTestUtils.setField(sensor, "id", SENSOR_ID);

        when(alertRepository.existsBySensorIdAndTypeAndStatusIn(eq(SENSOR_ID), eq(AlertType.GAS_DETECTED),
                anyCollection())).thenReturn(true);

        var response = alertService.createAutomaticAlertIfAbsent(
                device,
                sensor,
                AlertType.GAS_DETECTED,
                AlertSeverity.CRITICAL,
                "Gas level exceeded threshold",
                Instant.parse("2026-06-04T01:00:00Z")
        );

        assertThat(response).isEmpty();
        verify(alertRepository, never()).save(any(Alert.class));
    }
}
