package io.github.imecuadorian.smartguardbackend.device.api;

public record DeviceRegistrationResponse(
        DeviceResponse device,
        String apiKey
) {
}
