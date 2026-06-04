package io.github.imecuadorian.smartguardbackend.security.api;

import io.github.imecuadorian.smartguardbackend.security.domain.UserStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateUserStatusRequest(
        @NotNull(message = "User status is required")
        UserStatus status
) {
}
