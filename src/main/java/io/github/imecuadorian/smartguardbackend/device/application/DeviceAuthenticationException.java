package io.github.imecuadorian.smartguardbackend.device.application;

import io.github.imecuadorian.smartguardbackend.shared.error.UnauthorizedException;

public class DeviceAuthenticationException extends UnauthorizedException {
    public DeviceAuthenticationException(String message) {
        super(message);
    }
}
