package io.github.imecuadorian.smartguardbackend.alert.api;

import io.github.imecuadorian.smartguardbackend.alert.application.AlertService;
import io.github.imecuadorian.smartguardbackend.alert.domain.AlertSeverity;
import io.github.imecuadorian.smartguardbackend.alert.domain.AlertStatus;
import io.github.imecuadorian.smartguardbackend.alert.domain.AlertType;
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

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AlertController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class AlertControllerTest {

    private static final UUID DEVICE_ID = UUID.fromString("f6f928d2-1d8c-4ab1-9b8d-ec1a3d2f4f64");
    private static final UUID ALERT_ID = UUID.fromString("46539ac7-a7cc-4212-bd06-16817b431917");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AlertService alertService;

    @Test
    void createAlertReturnsCreatedAlert() throws Exception {
        when(alertService.create(any(CreateAlertRequest.class))).thenReturn(new AlertResponse(
                ALERT_ID,
                DEVICE_ID,
                null,
                AlertType.GAS_DETECTED,
                AlertSeverity.CRITICAL,
                AlertStatus.OPEN,
                "Gas level exceeded threshold",
                Instant.parse("2026-06-03T20:00:00Z"),
                null,
                null,
                null
        ));

        mockMvc.perform(post("/api/v1/alerts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "deviceId", DEVICE_ID,
                                "type", "GAS_DETECTED",
                                "severity", "CRITICAL",
                                "message", "Gas level exceeded threshold"
                        ))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("OPEN"))
                .andExpect(jsonPath("$.severity").value("CRITICAL"));
    }

    @Test
    void listAlertsReturnsAlerts() throws Exception {
        when(alertService.findAll(null, null)).thenReturn(List.of(new AlertResponse(
                ALERT_ID,
                DEVICE_ID,
                null,
                AlertType.ACCESS_DENIED,
                AlertSeverity.WARNING,
                AlertStatus.OPEN,
                "Unknown card",
                Instant.parse("2026-06-03T20:00:00Z"),
                null,
                null,
                null
        )));

        mockMvc.perform(get("/api/v1/alerts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void acknowledgeAlertReturnsAcknowledgedAlert() throws Exception {
        when(alertService.acknowledge(ALERT_ID)).thenReturn(new AlertResponse(
                ALERT_ID,
                DEVICE_ID,
                null,
                AlertType.ACCESS_DENIED,
                AlertSeverity.WARNING,
                AlertStatus.ACKNOWLEDGED,
                "Unknown card",
                Instant.parse("2026-06-03T20:00:00Z"),
                null,
                Instant.parse("2026-06-03T20:05:00Z"),
                null
        ));

        mockMvc.perform(patch("/api/v1/alerts/{id}/acknowledge", ALERT_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACKNOWLEDGED"));
    }
}
