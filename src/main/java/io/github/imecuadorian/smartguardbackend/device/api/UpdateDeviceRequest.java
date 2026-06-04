package io.github.imecuadorian.smartguardbackend.device.api;

import jakarta.validation.constraints.Size;

public record UpdateDeviceRequest(
        @Size(min = 1, max = 80, message = "Device code must have between 1 and 80 characters")
        String code,

        @Size(min = 1, max = 120, message = "Device name must have between 1 and 120 characters")
        String name,

        @Size(max = 160, message = "Device location must have at most 160 characters")
        String location,

        @Size(max = 45, message = "Device IP address must have at most 45 characters")
        String ipAddress,

        @Size(max = 40, message = "Device firmware version must have at most 40 characters")
        String firmwareVersion
) {
}
