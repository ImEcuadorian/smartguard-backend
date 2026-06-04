package io.github.imecuadorian.smartguardbackend.access.api;

import io.github.imecuadorian.smartguardbackend.access.domain.AccessReaderType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CreateAccessReaderRequest(
        @NotNull(message = "Device id is required")
        UUID deviceId,

        @NotBlank(message = "Access reader code is required")
        @Size(max = 80, message = "Access reader code must have at most 80 characters")
        String code,

        @NotNull(message = "Access reader type is required")
        AccessReaderType type,

        @Size(max = 160, message = "Access reader location must have at most 160 characters")
        String location
) {
}
