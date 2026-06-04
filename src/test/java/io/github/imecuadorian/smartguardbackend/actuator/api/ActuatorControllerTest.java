package io.github.imecuadorian.smartguardbackend.actuator.api;

import io.github.imecuadorian.smartguardbackend.actuator.application.ActuatorService;
import io.github.imecuadorian.smartguardbackend.actuator.domain.ActuatorCommandStatus;
import io.github.imecuadorian.smartguardbackend.actuator.domain.ActuatorCommandType;
import io.github.imecuadorian.smartguardbackend.actuator.domain.ActuatorStatus;
import io.github.imecuadorian.smartguardbackend.actuator.domain.ActuatorType;
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

import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ActuatorController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class ActuatorControllerTest {

    private static final UUID DEVICE_ID = UUID.fromString("f6f928d2-1d8c-4ab1-9b8d-ec1a3d2f4f64");
    private static final UUID ACTUATOR_ID = UUID.fromString("e7f90b8d-1bf4-45aa-a2fd-5dcd290a98dc");
    private static final UUID COMMAND_ID = UUID.fromString("3a402b65-80ac-49c1-9d07-d1d6e5a2f127");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ActuatorService actuatorService;

    @Test
    void createActuatorReturnsCreatedActuator() throws Exception {
        when(actuatorService.create(any(CreateActuatorRequest.class))).thenReturn(new ActuatorResponse(
                ACTUATOR_ID,
                DEVICE_ID,
                "relay-door",
                "Door relay",
                ActuatorType.RELAY,
                "Main entrance",
                ActuatorStatus.ACTIVE,
                null,
                null
        ));

        mockMvc.perform(post("/api/v1/actuators")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "deviceId", DEVICE_ID,
                                "code", "relay-door",
                                "name", "Door relay",
                                "type", "RELAY",
                                "location", "Main entrance"
                        ))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value("relay-door"))
                .andExpect(jsonPath("$.type").value("RELAY"));
    }

    @Test
    void createCommandReturnsPendingCommand() throws Exception {
        when(actuatorService.createCommand(eq(ACTUATOR_ID), any(CreateActuatorCommandRequest.class))).thenReturn(
                new ActuatorCommandResponse(
                        COMMAND_ID,
                        ACTUATOR_ID,
                        DEVICE_ID,
                        ActuatorCommandType.OPEN_DOOR,
                        ActuatorCommandStatus.PENDING,
                        "Open for authorized card",
                        null,
                        null
                )
        );

        mockMvc.perform(post("/api/v1/actuators/{id}/commands", ACTUATOR_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "command", "OPEN_DOOR",
                                "payload", "Open for authorized card"
                        ))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.command").value("OPEN_DOOR"))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }
}
