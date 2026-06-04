package io.github.imecuadorian.smartguardbackend.access.api;

import io.github.imecuadorian.smartguardbackend.access.domain.RfidCardStatus;

import java.time.Instant;
import java.util.UUID;

public record RfidCardResponse(
        UUID id,
        String uid,
        String ownerName,
        RfidCardStatus status,
        Instant createdAt,
        Instant updatedAt
) {
}
