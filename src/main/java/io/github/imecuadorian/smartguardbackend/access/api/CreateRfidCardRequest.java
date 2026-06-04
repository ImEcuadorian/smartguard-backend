package io.github.imecuadorian.smartguardbackend.access.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateRfidCardRequest(
        @NotBlank(message = "RFID card UID is required")
        @Size(max = 80, message = "RFID card UID must have at most 80 characters")
        String uid,

        @NotBlank(message = "RFID card owner name is required")
        @Size(max = 120, message = "RFID card owner name must have at most 120 characters")
        String ownerName
) {
}
