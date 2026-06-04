package io.github.imecuadorian.smartguardbackend.security.application;

@FunctionalInterface
public interface RefreshTokenGenerator {
    String generate();
}
