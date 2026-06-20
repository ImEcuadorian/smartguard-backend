package io.github.imecuadorian.smartguardbackend.security.application;

import io.github.imecuadorian.smartguardbackend.security.api.AuthResponse;
import io.github.imecuadorian.smartguardbackend.security.api.BootstrapAdminRequest;
import io.github.imecuadorian.smartguardbackend.security.api.ChangePasswordRequest;
import io.github.imecuadorian.smartguardbackend.security.api.LoginRequest;
import io.github.imecuadorian.smartguardbackend.security.api.LogoutRequest;
import io.github.imecuadorian.smartguardbackend.security.api.RefreshTokenRequest;
import io.github.imecuadorian.smartguardbackend.security.api.UserAccountMapper;
import io.github.imecuadorian.smartguardbackend.security.api.UserAccountResponse;
import io.github.imecuadorian.smartguardbackend.security.domain.RefreshToken;
import io.github.imecuadorian.smartguardbackend.security.domain.UserAccount;
import io.github.imecuadorian.smartguardbackend.security.domain.UserRole;
import io.github.imecuadorian.smartguardbackend.security.infrastructure.RefreshTokenRepository;
import io.github.imecuadorian.smartguardbackend.security.infrastructure.UserAccountRepository;
import io.github.imecuadorian.smartguardbackend.shared.error.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.HexFormat;

@Service
@Transactional
public class AuthService {

    private final UserAccountRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenGenerator refreshTokenGenerator;
    private final Clock clock;
    private final Duration refreshTokenExpiration;
    private final UserAccountMapper userAccountMapper;

    @Autowired
    public AuthService(UserAccountRepository userRepository, RefreshTokenRepository refreshTokenRepository,
                       PasswordEncoder passwordEncoder, JwtService jwtService,
                       RefreshTokenGenerator refreshTokenGenerator,
                       @Value("${smartguard.jwt.refresh-expiration-days}") long refreshExpirationDays,
                       UserAccountMapper userAccountMapper) {
        this(
                userRepository,
                refreshTokenRepository,
                passwordEncoder,
                jwtService,
                refreshTokenGenerator,
                Clock.systemUTC(),
                Duration.ofDays(refreshExpirationDays),
                userAccountMapper
        );
    }

    public AuthService(UserAccountRepository userRepository, RefreshTokenRepository refreshTokenRepository,
                       PasswordEncoder passwordEncoder, JwtService jwtService,
                       RefreshTokenGenerator refreshTokenGenerator, Clock clock, Duration refreshTokenExpiration) {
        this(userRepository, refreshTokenRepository, passwordEncoder, jwtService, refreshTokenGenerator, clock,
                refreshTokenExpiration, new UserAccountMapper());
    }

    public AuthService(UserAccountRepository userRepository, RefreshTokenRepository refreshTokenRepository,
                       PasswordEncoder passwordEncoder, JwtService jwtService,
                       RefreshTokenGenerator refreshTokenGenerator, Clock clock, Duration refreshTokenExpiration,
                       UserAccountMapper userAccountMapper) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.refreshTokenGenerator = refreshTokenGenerator;
        this.clock = clock;
        this.refreshTokenExpiration = refreshTokenExpiration;
        this.userAccountMapper = userAccountMapper;
    }

    public AuthResponse bootstrapAdmin(BootstrapAdminRequest request) {
        if (userRepository.count() > 0) {
            throw new ForbiddenOperationException("Bootstrap admin is only available before users exist");
        }

        var user = new UserAccount(
                request.username(),
                passwordEncoder.encode(request.password()),
                request.displayName(),
                UserRole.ADMIN
        );
        return authResponse(userRepository.save(user));
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        var user = userRepository.findByUsername(request.username().trim())
                .orElseThrow(() -> new AuthenticationFailedException("Invalid username or password"));

        if (!user.isActive() || !passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new AuthenticationFailedException("Invalid username or password");
        }

        return authResponse(user);
    }

    public AuthResponse refresh(RefreshTokenRequest request) {
        var storedToken = refreshTokenRepository.findByTokenHash(hashRefreshToken(request.refreshToken()))
                .orElseThrow(() -> new AuthenticationFailedException("Invalid refresh token"));

        if (storedToken.isRevoked() || storedToken.isExpired(Instant.now(clock)) || !storedToken.getUser().isActive()) {
            throw new AuthenticationFailedException("Invalid refresh token");
        }

        storedToken.revoke(Instant.now(clock));
        return authResponse(storedToken.getUser());
    }

    public void logout(LogoutRequest request) {
        refreshTokenRepository.findByTokenHash(hashRefreshToken(request.refreshToken()))
                .ifPresent(token -> token.revoke(Instant.now(clock)));
    }

    @Transactional(readOnly = true)
    public UserAccountResponse me(String username) {
        return userAccountMapper.toResponse(findActiveUser(username));
    }

    public UserAccountResponse changePassword(String username, ChangePasswordRequest request) {
        var user = findActiveUser(username);
        if (!passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
            throw new AuthenticationFailedException("Invalid current password");
        }
        user.changePassword(passwordEncoder.encode(request.newPassword()));
        return userAccountMapper.toResponse(user);
    }

    public static String hashRefreshToken(String refreshToken) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(refreshToken.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (Exception exception) {
            throw new IllegalStateException("Could not hash refresh token", exception);
        }
    }

    private AuthResponse authResponse(UserAccount user) {
        String refreshToken = refreshTokenGenerator.generate();
        refreshTokenRepository.save(new RefreshToken(
                user,
                hashRefreshToken(refreshToken),
                Instant.now(clock).plus(refreshTokenExpiration)
        ));
        return new AuthResponse(
                "Bearer",
                jwtService.createAccessToken(user.getUsername(), user.getRole()),
                refreshToken,
                jwtService.expirationMinutes(),
                user.getUsername(),
                user.getRole()
        );
    }

    private UserAccount findActiveUser(String username) {
        var user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        if (!user.isActive()) {
            throw new AuthenticationFailedException("User is disabled");
        }
        return user;
    }
}
