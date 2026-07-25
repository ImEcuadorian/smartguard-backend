package io.github.imecuadorian.smartguardbackend.security.api;

import io.github.imecuadorian.smartguardbackend.security.application.AuthService;
import io.github.imecuadorian.smartguardbackend.security.application.JwtService;
import io.github.imecuadorian.smartguardbackend.security.config.SecurityConfig;
import io.github.imecuadorian.smartguardbackend.security.domain.UserRole;
import io.github.imecuadorian.smartguardbackend.shared.error.GlobalExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private JwtService jwtService;

    @Test
    void publicRegistrationCreatesViewerWithoutAuthentication() throws Exception {
        when(authService.registerClient(any(RegisterClientRequest.class))).thenReturn(
                new AuthResponse(
                        "Bearer",
                        "access-token",
                        "refresh-token",
                        60,
                        "client@example.com",
                        UserRole.VIEWER
                )
        );

        mockMvc.perform(post("/api/v1/auth/register-client")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "displayName": "Client Account",
                                  "email": "client@example.com",
                                  "password": "strong-password"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accessToken").value("access-token"))
                .andExpect(jsonPath("$.refreshToken").value("refresh-token"))
                .andExpect(jsonPath("$.username").value("client@example.com"))
                .andExpect(jsonPath("$.role").value("VIEWER"));
    }
}
