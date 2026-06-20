package io.github.imecuadorian.smartguardbackend.security.application;

import io.github.imecuadorian.smartguardbackend.security.api.BootstrapAdminRequest;
import io.github.imecuadorian.smartguardbackend.security.api.LoginRequest;
import io.github.imecuadorian.smartguardbackend.security.api.RefreshTokenRequest;
import io.github.imecuadorian.smartguardbackend.security.domain.RefreshToken;
import io.github.imecuadorian.smartguardbackend.security.domain.UserAccount;
import io.github.imecuadorian.smartguardbackend.security.domain.UserRole;
import io.github.imecuadorian.smartguardbackend.security.infrastructure.RefreshTokenRepository;
import io.github.imecuadorian.smartguardbackend.security.infrastructure.UserAccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserAccountRepository userRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(
                userRepository,
                refreshTokenRepository,
                passwordEncoder,
                jwtService,
                () -> "refresh-token",
                Clock.fixed(Instant.parse("2026-06-04T12:00:00Z"), ZoneOffset.UTC),
                Duration.ofDays(7)
        );
    }

    @Test
    void bootstrapAdminCreatesFirstAdminAccount() {
        when(userRepository.count()).thenReturn(0L);
        when(passwordEncoder.encode("strong-password")).thenReturn("hash");
        when(userRepository.save(any(UserAccount.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(jwtService.createAccessToken("admin", UserRole.ADMIN)).thenReturn("token");
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = authService.bootstrapAdmin(new BootstrapAdminRequest(
                "admin",
                "strong-password",
                "Administrator"
        ));

        assertThat(response.accessToken()).isEqualTo("token");
        assertThat(response.refreshToken()).isEqualTo("refresh-token");
        assertThat(response.role()).isEqualTo(UserRole.ADMIN);
    }

    @Test
    void loginReturnsJwtForValidCredentials() {
        var user = new UserAccount("admin", "hash", "Administrator", UserRole.ADMIN);

        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("strong-password", "hash")).thenReturn(true);
        when(jwtService.createAccessToken("admin", UserRole.ADMIN)).thenReturn("token");
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = authService.login(new LoginRequest("admin", "strong-password"));

        assertThat(response.accessToken()).isEqualTo("token");
        assertThat(response.refreshToken()).isEqualTo("refresh-token");
        assertThat(response.username()).isEqualTo("admin");
    }

    @Test
    void loginUsesWritableTransactionBecauseItCreatesRefreshToken() throws Exception {
        var transaction = AuthService.class.getDeclaredMethod("login", LoginRequest.class)
                .getAnnotation(Transactional.class);

        assertThat(transaction).isNotNull();
        assertThat(transaction.readOnly()).isFalse();
    }

    @Test
    void refreshReturnsNewAccessAndRefreshTokenForValidRefreshToken() {
        var user = new UserAccount("admin", "hash", "Administrator", UserRole.ADMIN);
        var storedToken = new RefreshToken(
                user,
                AuthService.hashRefreshToken("old-refresh-token"),
                Instant.parse("2026-06-11T12:00:00Z")
        );

        when(refreshTokenRepository.findByTokenHash(AuthService.hashRefreshToken("old-refresh-token")))
                .thenReturn(Optional.of(storedToken));
        when(jwtService.createAccessToken("admin", UserRole.ADMIN)).thenReturn("new-access-token");
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = authService.refresh(new RefreshTokenRequest("old-refresh-token"));

        assertThat(response.accessToken()).isEqualTo("new-access-token");
        assertThat(response.refreshToken()).isEqualTo("refresh-token");
        assertThat(storedToken.isRevoked()).isTrue();
    }
}
