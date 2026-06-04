package io.github.imecuadorian.smartguardbackend.device.api;

import io.github.imecuadorian.smartguardbackend.device.domain.DeviceStatus;

import java.time.Instant;
import java.util.UUID;

public record DeviceResponse(
        UUID id,
        String code,
        String name,
        String location,
        DeviceStatus status,
        String ipAddress,
        String firmwareVersion,
        Instant lastSeenAt,
        Instant createdAt,
        Instant updatedAt
) {
}
