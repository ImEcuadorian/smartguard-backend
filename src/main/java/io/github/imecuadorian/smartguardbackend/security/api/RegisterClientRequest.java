package io.github.imecuadorian.smartguardbackend.security.api;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterClientRequest(
        @NotBlank(message = "Display name is required")
        @Size(max = 120, message = "Display name must have at most 120 characters")
        String displayName,

        @NotBlank(message = "Email is required")
        @Email(message = "Email must be valid")
        @Size(max = 80, message = "Email must have at most 80 characters")
        String email,

        @NotBlank(message = "Password is required")
        @Size(min = 8, max = 80, message = "Password must have between 8 and 80 characters")
        String password
) {
}
