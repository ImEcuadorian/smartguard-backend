package io.github.imecuadorian.smartguardbackend.shared.error;

public class UnauthorizedException extends RuntimeException {
    public UnauthorizedException(String message) {
        super(message);
    }
}
