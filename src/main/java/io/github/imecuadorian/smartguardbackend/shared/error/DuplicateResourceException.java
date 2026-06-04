package io.github.imecuadorian.smartguardbackend.shared.error;

public class DuplicateResourceException extends RuntimeException {
    public DuplicateResourceException(String message) {
        super(message);
    }
}
