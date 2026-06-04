package io.github.imecuadorian.smartguardbackend.device.api;

import io.github.imecuadorian.smartguardbackend.device.application.DeviceService;
import io.github.imecuadorian.smartguardbackend.device.domain.DeviceStatus;
import io.github.imecuadorian.smartguardbackend.shared.error.GlobalExceptionHandler;
import io.github.imecuadorian.smartguardbackend.shared.error.ResourceNotFoundException;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
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

@WebMvcTest(DeviceController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class DeviceControllerTest {

    private static final UUID DEVICE_ID = UUID.fromString("f6f928d2-1d8c-4ab1-9b8d-ec1a3d2f4f64");
    private static final Instant CREATED_AT = Instant.parse("2026-06-03T20:00:00Z");
    private static final Instant UPDATED_AT = Instant.parse("2026-06-03T20:05:00Z");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private DeviceService deviceService;

    @Test
    void createDeviceReturnsCreatedDevice() throws Exception {
        var deviceResponse = new DeviceResponse(
                DEVICE_ID,
                "esp32-001",
                "ESP32 Main Door",
                "Main entrance",
                DeviceStatus.ACTIVE,
                "192.168.1.50",
                "1.0.0",
                null,
                CREATED_AT,
                UPDATED_AT
        );
        var response = new DeviceRegistrationResponse(deviceResponse, "device-api-key");

        when(deviceService.create(any(CreateDeviceRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/devices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "code", "esp32-001",
                                "name", "ESP32 Main Door",
                                "location", "Main entrance",
                                "ipAddress", "192.168.1.50",
                                "firmwareVersion", "1.0.0"
                        ))))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/v1/devices/" + DEVICE_ID))
                .andExpect(jsonPath("$.device.id").value(DEVICE_ID.toString()))
                .andExpect(jsonPath("$.device.code").value("esp32-001"))
                .andExpect(jsonPath("$.device.status").value("ACTIVE"))
                .andExpect(jsonPath("$.apiKey").value("device-api-key"));
    }

    @Test
    void createDeviceRejectsBlankCode() throws Exception {
        mockMvc.perform(post("/api/v1/devices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "code", "",
                                "name", "ESP32 Main Door"
                        ))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.validation.code").value("Device code is required"));
    }

    @Test
    void listDevicesReturnsDevices() throws Exception {
        when(deviceService.findAll(null)).thenReturn(List.of(new DeviceResponse(
                DEVICE_ID,
                "esp32-001",
                "ESP32 Main Door",
                "Main entrance",
                DeviceStatus.ACTIVE,
                "192.168.1.50",
                "1.0.0",
                null,
                CREATED_AT,
                UPDATED_AT
        )));

        mockMvc.perform(get("/api/v1/devices"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].code").value("esp32-001"));
    }

    @Test
    void findDeviceByIdReturnsNotFoundWhenMissing() throws Exception {
        when(deviceService.findById(DEVICE_ID)).thenThrow(new ResourceNotFoundException("Device not found"));

        mockMvc.perform(get("/api/v1/devices/{id}", DEVICE_ID))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Device not found"));
    }

    @Test
    void updateDeviceStatusReturnsUpdatedDevice() throws Exception {
        var response = new DeviceResponse(
                DEVICE_ID,
                "esp32-001",
                "ESP32 Main Door",
                "Main entrance",
                DeviceStatus.MAINTENANCE,
                "192.168.1.50",
                "1.0.0",
                null,
                CREATED_AT,
                UPDATED_AT
        );

        when(deviceService.updateStatus(eq(DEVICE_ID), any(UpdateDeviceStatusRequest.class))).thenReturn(response);

        mockMvc.perform(patch("/api/v1/devices/{id}/status", DEVICE_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("status", "MAINTENANCE"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(DEVICE_ID.toString()))
                .andExpect(jsonPath("$.status").value("MAINTENANCE"));
    }
}
