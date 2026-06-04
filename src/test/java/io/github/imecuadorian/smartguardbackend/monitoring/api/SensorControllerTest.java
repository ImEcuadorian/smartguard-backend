package io.github.imecuadorian.smartguardbackend.monitoring.api;

import io.github.imecuadorian.smartguardbackend.monitoring.application.SensorService;
import io.github.imecuadorian.smartguardbackend.monitoring.domain.SensorStatus;
import io.github.imecuadorian.smartguardbackend.monitoring.domain.SensorType;
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
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SensorController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class SensorControllerTest {

    private static final UUID DEVICE_ID = UUID.fromString("f6f928d2-1d8c-4ab1-9b8d-ec1a3d2f4f64");
    private static final UUID SENSOR_ID = UUID.fromString("aabdb5ce-9531-42e6-a43e-4368cc9dca9e");
    private static final UUID READING_ID = UUID.fromString("89cb0388-61f0-4c68-b0c2-6dcfdc74ab80");
    private static final Instant RECORDED_AT = Instant.parse("2026-06-03T20:00:00Z");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private SensorService sensorService;

    @Test
    void createSensorReturnsCreatedSensor() throws Exception {
        when(sensorService.create(any(CreateSensorRequest.class))).thenReturn(new SensorResponse(
                SENSOR_ID,
                DEVICE_ID,
                "temp-001",
                "Main temperature",
                SensorType.TEMPERATURE,
                "celsius",
                "Main entrance",
                SensorStatus.ACTIVE,
                null,
                null,
                null
        ));

        mockMvc.perform(post("/api/v1/sensors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "deviceId", DEVICE_ID,
                                "code", "temp-001",
                                "name", "Main temperature",
                                "type", "TEMPERATURE",
                                "unit", "celsius",
                                "location", "Main entrance"
                        ))))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/v1/sensors/" + SENSOR_ID))
                .andExpect(jsonPath("$.code").value("temp-001"))
                .andExpect(jsonPath("$.type").value("TEMPERATURE"));
    }

    @Test
    void createSensorRejectsMissingDeviceId() throws Exception {
        mockMvc.perform(post("/api/v1/sensors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "code", "temp-001",
                                "name", "Main temperature",
                                "type", "TEMPERATURE"
                        ))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validation.deviceId").value("Device id is required"));
    }

    @Test
    void listSensorsReturnsSensors() throws Exception {
        when(sensorService.findAll(null, null, null)).thenReturn(List.of(new SensorResponse(
                SENSOR_ID,
                DEVICE_ID,
                "temp-001",
                "Main temperature",
                SensorType.TEMPERATURE,
                "celsius",
                "Main entrance",
                SensorStatus.ACTIVE,
                null,
                null,
                null
        )));

        mockMvc.perform(get("/api/v1/sensors"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].code").value("temp-001"));
    }

    @Test
    void createReadingReturnsCreatedReading() throws Exception {
        when(sensorService.createReading(eq(SENSOR_ID), any(CreateSensorReadingRequest.class))).thenReturn(
                new SensorReadingResponse(
                        READING_ID,
                        SENSOR_ID,
                        DEVICE_ID,
                        new BigDecimal("25.6"),
                        null,
                        null,
                        RECORDED_AT,
                        null
                )
        );

        mockMvc.perform(post("/api/v1/sensors/{id}/readings", SENSOR_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "numericValue", new BigDecimal("25.6"),
                                "recordedAt", RECORDED_AT
                        ))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.sensorId").value(SENSOR_ID.toString()))
                .andExpect(jsonPath("$.numericValue").value(25.6));
    }

    @Test
    void listReadingsAcceptsDateRangeAndLimitForCharts() throws Exception {
        when(sensorService.findReadings(
                eq(SENSOR_ID),
                eq(Instant.parse("2026-06-04T00:00:00Z")),
                eq(Instant.parse("2026-06-04T02:00:00Z")),
                eq(50)
        )).thenReturn(List.of(new SensorReadingResponse(
                READING_ID,
                SENSOR_ID,
                DEVICE_ID,
                new BigDecimal("25.6"),
                null,
                null,
                RECORDED_AT,
                null
        )));

        mockMvc.perform(get("/api/v1/sensors/{id}/readings", SENSOR_ID)
                        .param("from", "2026-06-04T00:00:00Z")
                        .param("to", "2026-06-04T02:00:00Z")
                        .param("limit", "50"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].numericValue").value(25.6));
    }

    @Test
    void findLatestReadingReturnsMostRecentReading() throws Exception {
        when(sensorService.findLatestReading(SENSOR_ID)).thenReturn(new SensorReadingResponse(
                READING_ID,
                SENSOR_ID,
                DEVICE_ID,
                new BigDecimal("25.6"),
                null,
                null,
                RECORDED_AT,
                null
        ));

        mockMvc.perform(get("/api/v1/sensors/{id}/readings/latest", SENSOR_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(READING_ID.toString()))
                .andExpect(jsonPath("$.numericValue").value(25.6));
    }
}
