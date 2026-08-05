package io.github.imecuadorian.smartguardbackend.bdd;

import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.github.imecuadorian.smartguardbackend.alert.application.AlertService;
import io.github.imecuadorian.smartguardbackend.alert.domain.AlertSeverity;
import io.github.imecuadorian.smartguardbackend.alert.domain.AlertType;
import io.github.imecuadorian.smartguardbackend.device.domain.Device;
import io.github.imecuadorian.smartguardbackend.monitoring.application.SensorAlertRuleEvaluator;
import io.github.imecuadorian.smartguardbackend.monitoring.domain.*;
import io.github.imecuadorian.smartguardbackend.monitoring.infrastructure.SensorAlertRuleRepository;
import io.github.imecuadorian.smartguardbackend.monitoring.infrastructure.SensorReadingRepository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

public class TemperatureAlertSteps {

    private SensorAlertRuleRepository ruleRepository;
    private SensorReadingRepository readingRepository;
    private AlertService alertService;
    private SensorAlertRuleEvaluator evaluator;

    private Device device;
    private Sensor sensor;
    private SensorReading reading;

    private String expectedAlertMessage;

    @Before
    public void setUp() {
        ruleRepository = mock(SensorAlertRuleRepository.class);
        readingRepository = mock(SensorReadingRepository.class);
        alertService = mock(AlertService.class);

        device = new Device(
                "esp32-001",
                "ESP32 SmartGuard",
                "Área principal",
                null,
                "1.0.0"
        );

        sensor = new Sensor(
                device,
                "temp-dht22-001",
                "Sensor de temperatura principal",
                SensorType.TEMPERATURE,
                "°C",
                "Área principal"
        );

        evaluator = new SensorAlertRuleEvaluator(
                ruleRepository,
                readingRepository,
                alertService
        );
    }

    @Given("que existe una regla activa con un umbral de {int} grados")
    public void existeUnaReglaActivaConUmbral(int threshold) {
        var rule = createTemperatureRule(threshold);

        when(
                ruleRepository.findAllBySensorIdAndEnabledTrue(sensor.getId())
        ).thenReturn(List.of(rule));
    }

    @Given("que existe una regla desactivada con un umbral de {int} grados")
    public void existeUnaReglaDesactivadaConUmbral(int threshold) {
        var rule = createTemperatureRule(threshold);
        rule.disable();

        /*
         * El repositorio consultado por el evaluador devuelve únicamente
         * reglas habilitadas. Una regla desactivada no debe ser encontrada.
         */
        when(
                ruleRepository.findAllBySensorIdAndEnabledTrue(sensor.getId())
        ).thenReturn(List.of());
    }

    @Given("el sensor se encuentra en mantenimiento")
    public void elSensorSeEncuentraEnMantenimiento() {
        sensor.updateStatus(SensorStatus.MAINTENANCE);
    }

    @When("el sensor registra una temperatura de {int} grados")
    public void elSensorRegistraUnaTemperatura(int temperature) {
        reading = new SensorReading(
                sensor,
                BigDecimal.valueOf(temperature),
                null,
                null,
                Instant.parse("2026-08-04T20:00:00Z")
        );

        evaluator.evaluate(reading);
    }

    @Then("el sistema debe generar una alerta de temperatura alta")
    public void elSistemaDebeGenerarUnaAlerta() {
        verify(alertService, times(1))
                .createAutomaticAlertIfAbsent(
                        eq(device),
                        eq(sensor),
                        eq(AlertType.THRESHOLD_EXCEEDED),
                        eq(AlertSeverity.WARNING),
                        eq(expectedAlertMessage),
                        eq(reading.getRecordedAt())
                );
    }

    @Then("el sistema no debe generar una alerta")
    public void elSistemaNoDebeGenerarUnaAlerta() {
        verifyNoInteractions(alertService);
    }

    private SensorAlertRule createTemperatureRule(int threshold) {
        expectedAlertMessage =
                "Temperatura superior a " + threshold + " °C";

        return new SensorAlertRule(
                sensor,
                SensorAlertRuleType.NUMERIC_THRESHOLD,
                ComparisonOperator.GREATER_THAN,
                BigDecimal.valueOf(threshold),
                null,
                null,
                AlertType.THRESHOLD_EXCEEDED,
                AlertSeverity.WARNING,
                expectedAlertMessage
        );
    }
}