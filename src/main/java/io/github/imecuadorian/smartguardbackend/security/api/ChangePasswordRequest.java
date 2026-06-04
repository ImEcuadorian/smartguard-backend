package io.github.imecuadorian.smartguardbackend.security.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChangePasswordRequest(
        @NotBlank(message = "Current password is required")
        String currentPassword,

        @NotBlank(message = "New password is required")
        @Size(min = 8, max = 80, message = "New password must have between 8 and 80 characters")
        String newPassword
) {
}
