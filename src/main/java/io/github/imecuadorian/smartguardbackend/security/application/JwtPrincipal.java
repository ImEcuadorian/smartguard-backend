package io.github.imecuadorian.smartguardbackend.security.application;

import io.github.imecuadorian.smartguardbackend.security.domain.UserRole;

public record JwtPrincipal(
        String username,
        UserRole role
) {
}
