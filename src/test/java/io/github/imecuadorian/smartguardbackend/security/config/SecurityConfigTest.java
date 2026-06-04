package io.github.imecuadorian.smartguardbackend.security.config;

import io.github.imecuadorian.smartguardbackend.device.api.DeviceController;
import io.github.imecuadorian.smartguardbackend.device.application.DeviceService;
import io.github.imecuadorian.smartguardbackend.security.application.JwtPrincipal;
import io.github.imecuadorian.smartguardbackend.security.application.JwtService;
import io.github.imecuadorian.smartguardbackend.security.domain.UserRole;
import io.github.imecuadorian.smartguardbackend.shared.error.GlobalExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DeviceController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DeviceService deviceService;

    @MockitoBean
    private JwtService jwtService;

    @Test
    void deviceApiRequiresAuthentication() throws Exception {
        when(deviceService.findAll(null)).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/devices"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void viewerCanReadButCannotCreateDevices() throws Exception {
        when(jwtService.parse("viewer-token")).thenReturn(new JwtPrincipal("viewer", UserRole.VIEWER));
        when(deviceService.findAll(null)).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/devices").header("Authorization", "Bearer viewer-token"))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/devices")
                        .header("Authorization", "Bearer viewer-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "code": "esp32-001",
                                  "name": "ESP32 Main Door"
                                }
                                """))
                .andExpect(status().isForbidden());
    }
}
