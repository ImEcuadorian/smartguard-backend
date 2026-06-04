package io.github.imecuadorian.smartguardbackend.security.application;

public class AuthenticationFailedException extends RuntimeException {
    public AuthenticationFailedException(String message) {
        super(message);
    }
}
