package io.github.imecuadorian.smartguardbackend.security.api;

import io.github.imecuadorian.smartguardbackend.security.domain.UserRole;
import io.github.imecuadorian.smartguardbackend.security.domain.UserStatus;

import java.time.Instant;
import java.util.UUID;

public record UserAccountResponse(
        UUID id,
        String username,
        String displayName,
        UserRole role,
        UserStatus status,
        Instant createdAt,
        Instant updatedAt
) {
}
