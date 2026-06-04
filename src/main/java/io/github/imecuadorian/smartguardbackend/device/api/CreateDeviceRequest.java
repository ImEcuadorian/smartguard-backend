package io.github.imecuadorian.smartguardbackend.device.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateDeviceRequest(
        @NotBlank(message = "Device code is required")
        @Size(max = 80, message = "Device code must have at most 80 characters")
        String code,

        @NotBlank(message = "Device name is required")
        @Size(max = 120, message = "Device name must have at most 120 characters")
        String name,

        @Size(max = 160, message = "Device location must have at most 160 characters")
        String location,

        @Size(max = 45, message = "Device IP address must have at most 45 characters")
        String ipAddress,

        @Size(max = 40, message = "Device firmware version must have at most 40 characters")
        String firmwareVersion
) {
}
