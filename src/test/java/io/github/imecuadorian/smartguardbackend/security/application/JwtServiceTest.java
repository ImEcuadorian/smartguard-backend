package io.github.imecuadorian.smartguardbackend.security.application;

import io.github.imecuadorian.smartguardbackend.security.domain.UserRole;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    @Test
    void createTokenCanBeParsedBackToAuthenticatedPrincipal() {
        var jwtService = new JwtService(
                "01234567890123456789012345678901",
                Duration.ofMinutes(60),
                new JsonMapper()
        );

        String token = jwtService.createAccessToken("admin", UserRole.ADMIN);

        var principal = jwtService.parse(token);

        assertThat(principal.username()).isEqualTo("admin");
        assertThat(principal.role()).isEqualTo(UserRole.ADMIN);
    }
}
