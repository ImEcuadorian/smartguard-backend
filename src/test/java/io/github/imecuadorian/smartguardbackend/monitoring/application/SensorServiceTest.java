package io.github.imecuadorian.smartguardbackend.monitoring.application;

import io.github.imecuadorian.smartguardbackend.device.domain.Device;
import io.github.imecuadorian.smartguardbackend.device.infrastructure.DeviceRepository;
import io.github.imecuadorian.smartguardbackend.monitoring.api.CreateSensorReadingRequest;
import io.github.imecuadorian.smartguardbackend.monitoring.api.CreateSensorRequest;
import io.github.imecuadorian.smartguardbackend.monitoring.api.SensorMapper;
import io.github.imecuadorian.smartguardbackend.monitoring.domain.Sensor;
import io.github.imecuadorian.smartguardbackend.monitoring.domain.SensorReading;
import io.github.imecuadorian.smartguardbackend.monitoring.domain.SensorStatus;
import io.github.imecuadorian.smartguardbackend.monitoring.domain.SensorType;
import io.github.imecuadorian.smartguardbackend.monitoring.infrastructure.SensorReadingRepository;
import io.github.imecuadorian.smartguardbackend.monitoring.infrastructure.SensorRepository;
import io.github.imecuadorian.smartguardbackend.shared.error.DuplicateResourceException;
import io.github.imecuadorian.smartguardbackend.shared.error.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SensorServiceTest {

    private static final UUID DEVICE_ID = UUID.fromString("f6f928d2-1d8c-4ab1-9b8d-ec1a3d2f4f64");
    private static final UUID SENSOR_ID = UUID.fromString("aabdb5ce-9531-42e6-a43e-4368cc9dca9e");

    @Mock
    private DeviceRepository deviceRepository;

    @Mock
    private SensorRepository sensorRepository;

    @Mock
    private SensorReadingRepository readingRepository;

    @Mock
    private SensorAlertRuleEvaluator alertRuleEvaluator;

    private SensorService sensorService;

    @BeforeEach
    void setUp() {
        sensorService = new SensorService(
                deviceRepository,
                sensorRepository,
                readingRepository,
                new SensorMapper(),
                alertRuleEvaluator
        );
    }

    @Test
    void createSensorStoresAnActiveSensorForADevice() {
        var device = new Device("esp32-001", "ESP32 Main Door", "Main entrance", null, null);
        var request = new CreateSensorRequest(
                DEVICE_ID,
                "temp-001",
                "Main temperature",
                SensorType.TEMPERATURE,
                "celsius",
                "Main entrance"
        );

        when(deviceRepository.findById(DEVICE_ID)).thenReturn(Optional.of(device));
        when(sensorRepository.existsByCode("temp-001")).thenReturn(false);
        when(sensorRepository.save(any(Sensor.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = sensorService.create(request);

        assertThat(response.code()).isEqualTo("temp-001");
        assertThat(response.status()).isEqualTo(SensorStatus.ACTIVE);
        assertThat(response.type()).isEqualTo(SensorType.TEMPERATURE);
        verify(sensorRepository).save(any(Sensor.class));
    }

    @Test
    void createSensorRejectsDuplicateCode() {
        var request = new CreateSensorRequest(
                DEVICE_ID,
                "temp-001",
                "Main temperature",
                SensorType.TEMPERATURE,
                "celsius",
                null
        );

        when(sensorRepository.existsByCode("temp-001")).thenReturn(true);

        assertThatThrownBy(() -> sensorService.create(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessage("Sensor code already exists");
    }

    @Test
    void createReadingRejectsMissingSensor() {
        var request = new CreateSensorReadingRequest(
                new BigDecimal("25.6"),
                null,
                null,
                Instant.parse("2026-06-03T20:00:00Z")
        );

        when(sensorRepository.findById(SENSOR_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sensorService.createReading(SENSOR_ID, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Sensor not found");
    }

    @Test
    void createReadingMarksSensorAsSeenAndEvaluatesAlertRules() {
        var device = new Device("esp32-001", "ESP32 Main Door", "Main entrance", null, null);
        var sensor = new Sensor(device, "gas-main", "Main gas sensor", SensorType.GAS, "ppm", null);
        var request = new CreateSensorReadingRequest(
                new BigDecimal("750"),
                null,
                null,
                Instant.parse("2026-06-04T01:00:00Z")
        );

        when(sensorRepository.findById(SENSOR_ID)).thenReturn(Optional.of(sensor));
        when(readingRepository.save(any(SensorReading.class))).thenAnswer(invocation -> invocation.getArgument(0));

        sensorService.createReading(SENSOR_ID, request);

        assertThat(sensor.getLastReadingAt()).isEqualTo(Instant.parse("2026-06-04T01:00:00Z"));
        verify(alertRuleEvaluator).evaluate(any(SensorReading.class));
    }

    @Test
    void findReadingsUsesDateRangeAndLimitForChartHistory() {
        var device = new Device("esp32-001", "ESP32 Main Door", "Main entrance", null, null);
        var sensor = new Sensor(device, "gas-main", "Main gas sensor", SensorType.GAS, "ppm", null);
        var from = Instant.parse("2026-06-04T00:00:00Z");
        var to = Instant.parse("2026-06-04T02:00:00Z");
        var reading = new SensorReading(sensor, new BigDecimal("650"), null, null,
                Instant.parse("2026-06-04T01:00:00Z"));

        when(sensorRepository.existsById(SENSOR_ID)).thenReturn(true);
        when(readingRepository.findBySensorIdAndRecordedAtBetweenOrderByRecordedAtDesc(
                eq(SENSOR_ID), eq(from), eq(to), any(Pageable.class)))
                .thenReturn(List.of(reading));

        var readings = sensorService.findReadings(SENSOR_ID, from, to, 50);

        assertThat(readings).hasSize(1);
        assertThat(readings.getFirst().numericValue()).isEqualByComparingTo("650");
    }

    @Test
    void findLatestReadingReturnsMostRecentReading() {
        var device = new Device("esp32-001", "ESP32 Main Door", "Main entrance", null, null);
        var sensor = new Sensor(device, "gas-main", "Main gas sensor", SensorType.GAS, "ppm", null);
        var reading = new SensorReading(sensor, new BigDecimal("650"), null, null,
                Instant.parse("2026-06-04T01:00:00Z"));

        when(sensorRepository.existsById(SENSOR_ID)).thenReturn(true);
        when(readingRepository.findFirstBySensorIdOrderByRecordedAtDesc(SENSOR_ID)).thenReturn(Optional.of(reading));

        var latest = sensorService.findLatestReading(SENSOR_ID);

        assertThat(latest.numericValue()).isEqualByComparingTo("650");
    }
}
