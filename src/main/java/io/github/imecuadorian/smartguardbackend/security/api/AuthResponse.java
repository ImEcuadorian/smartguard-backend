package io.github.imecuadorian.smartguardbackend.security.api;

import io.github.imecuadorian.smartguardbackend.security.domain.UserRole;

public record AuthResponse(
        String tokenType,
        String accessToken,
        String refreshToken,
        long expiresInMinutes,
        String username,
        UserRole role
) {
}
