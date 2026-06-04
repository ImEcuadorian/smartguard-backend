package io.github.imecuadorian.smartguardbackend.shared.error;

public class InvalidRequestException extends RuntimeException {
    public InvalidRequestException(String message) {
        super(message);
    }
}
