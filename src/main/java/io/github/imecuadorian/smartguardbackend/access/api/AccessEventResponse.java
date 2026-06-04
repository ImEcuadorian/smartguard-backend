package io.github.imecuadorian.smartguardbackend.access.api;

import io.github.imecuadorian.smartguardbackend.access.domain.AccessResult;

import java.time.Instant;
import java.util.UUID;

public record AccessEventResponse(
        UUID id,
        UUID deviceId,
        UUID readerId,
        UUID cardId,
        String cardUid,
        AccessResult result,
        String reason,
        Instant occurredAt,
        Instant createdAt
) {
}
