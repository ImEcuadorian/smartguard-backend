package io.github.imecuadorian.smartguardbackend.device.application;

@FunctionalInterface
public interface DeviceApiKeyGenerator {
    String generate();
}
