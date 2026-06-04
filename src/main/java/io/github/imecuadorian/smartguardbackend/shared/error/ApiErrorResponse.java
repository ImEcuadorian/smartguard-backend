package io.github.imecuadorian.smartguardbackend.shared.error;

import java.time.Instant;
import java.util.Map;

public record ApiErrorResponse(
        Instant timestamp,
        int status,
        String error,
        String message,
        String path,
        Map<String, String> validation
) {
    public static ApiErrorResponse of(int status, String error, String message, String path) {
        return new ApiErrorResponse(Instant.now(), status, error, message, path, Map.of());
    }

    public static ApiErrorResponse validation(int status, String error, String message, String path,
                                              Map<String, String> validation) {
        return new ApiErrorResponse(Instant.now(), status, error, message, path, validation);
    }
}
