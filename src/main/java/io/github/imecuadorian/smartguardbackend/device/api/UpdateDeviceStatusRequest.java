package io.github.imecuadorian.smartguardbackend.device.api;

import io.github.imecuadorian.smartguardbackend.device.domain.DeviceStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateDeviceStatusRequest(
        @NotNull(message = "Device status is required")
        DeviceStatus status
) {
}
