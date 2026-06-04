package io.github.imecuadorian.smartguardbackend.security.api;

import io.github.imecuadorian.smartguardbackend.security.domain.UserRole;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateUserRequest(
        @NotBlank(message = "Username is required")
        @Size(max = 80, message = "Username must have at most 80 characters")
        String username,

        @NotBlank(message = "Password is required")
        @Size(min = 8, max = 80, message = "Password must have between 8 and 80 characters")
        String password,

        @NotBlank(message = "Display name is required")
        @Size(max = 120, message = "Display name must have at most 120 characters")
        String displayName,

        @NotNull(message = "User role is required")
        UserRole role
) {
}
