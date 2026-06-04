package io.github.imecuadorian.smartguardbackend.monitoring.api;

import io.github.imecuadorian.smartguardbackend.alert.domain.AlertSeverity;
import io.github.imecuadorian.smartguardbackend.alert.domain.AlertType;
import io.github.imecuadorian.smartguardbackend.monitoring.application.SensorAlertRuleService;
import io.github.imecuadorian.smartguardbackend.monitoring.domain.ComparisonOperator;
import io.github.imecuadorian.smartguardbackend.monitoring.domain.SensorAlertRuleType;
import io.github.imecuadorian.smartguardbackend.shared.error.GlobalExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SensorAlertRuleController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class SensorAlertRuleControllerTest {

    private static final UUID SENSOR_ID = UUID.fromString("aabdb5ce-9531-42e6-a43e-4368cc9dca9e");
    private static final UUID RULE_ID = UUID.fromString("6047c7ec-75ee-41d1-a911-06f9ca34319f");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private SensorAlertRuleService ruleService;

    @Test
    void createRuleReturnsCreatedRule() throws Exception {
        when(ruleService.create(eq(SENSOR_ID), any(CreateSensorAlertRuleRequest.class))).thenReturn(ruleResponse(true));

        mockMvc.perform(post("/api/v1/sensors/{sensorId}/alert-rules", SENSOR_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "type", "NUMERIC_THRESHOLD",
                                "operator", "GREATER_THAN",
                                "thresholdValue", new BigDecimal("700"),
                                "alertType", "GAS_DETECTED",
                                "severity", "CRITICAL",
                                "message", "Gas level exceeded 700 ppm"
                        ))))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/v1/sensor-alert-rules/" + RULE_ID))
                .andExpect(jsonPath("$.id").value(RULE_ID.toString()))
                .andExpect(jsonPath("$.type").value("NUMERIC_THRESHOLD"))
                .andExpect(jsonPath("$.enabled").value(true));
    }

    @Test
    void listRulesReturnsRulesForSensor() throws Exception {
        when(ruleService.findBySensor(SENSOR_ID)).thenReturn(List.of(ruleResponse(true)));

        mockMvc.perform(get("/api/v1/sensors/{sensorId}/alert-rules", SENSOR_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].thresholdValue").value(700));
    }

    @Test
    void disableRuleReturnsDisabledRule() throws Exception {
        when(ruleService.disable(RULE_ID)).thenReturn(ruleResponse(false));

        mockMvc.perform(patch("/api/v1/sensor-alert-rules/{id}/disable", RULE_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.enabled").value(false));
    }

    private SensorAlertRuleResponse ruleResponse(boolean enabled) {
        return new SensorAlertRuleResponse(
                RULE_ID,
                SENSOR_ID,
                SensorAlertRuleType.NUMERIC_THRESHOLD,
                ComparisonOperator.GREATER_THAN,
                new BigDecimal("700"),
                null,
                null,
                AlertType.GAS_DETECTED,
                AlertSeverity.CRITICAL,
                "Gas level exceeded 700 ppm",
                enabled,
                null,
                null
        );
    }
}
