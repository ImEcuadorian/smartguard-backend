package io.github.imecuadorian.smartguardbackend.security.application;

public class ForbiddenOperationException extends RuntimeException {
    public ForbiddenOperationException(String message) {
        super(message);
    }
}
