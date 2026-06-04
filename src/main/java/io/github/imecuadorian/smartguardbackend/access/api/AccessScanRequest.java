package io.github.imecuadorian.smartguardbackend.access.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.Instant;

public record AccessScanRequest(
        @NotBlank(message = "Access reader code is required")
        @Size(max = 80, message = "Access reader code must have at most 80 characters")
        String readerCode,

        @NotBlank(message = "RFID card UID is required")
        @Size(max = 80, message = "RFID card UID must have at most 80 characters")
        String cardUid,

        Instant occurredAt
) {
}
