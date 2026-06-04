package io.github.imecuadorian.smartguardbackend.access.api;

import io.github.imecuadorian.smartguardbackend.access.domain.AccessReaderStatus;
import io.github.imecuadorian.smartguardbackend.access.domain.AccessReaderType;

import java.time.Instant;
import java.util.UUID;

public record AccessReaderResponse(
        UUID id,
        UUID deviceId,
        String code,
        AccessReaderType type,
        String location,
        AccessReaderStatus status,
        Instant createdAt,
        Instant updatedAt
) {
}
