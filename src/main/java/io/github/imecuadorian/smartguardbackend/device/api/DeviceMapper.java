package io.github.imecuadorian.smartguardbackend.device.api;

import io.github.imecuadorian.smartguardbackend.device.domain.Device;
import org.springframework.stereotype.Component;

@Component
public class DeviceMapper {
    public DeviceResponse toResponse(Device device) {
        return new DeviceResponse(
                device.getId(),
                device.getCode(),
                device.getName(),
                device.getLocation(),
                device.getStatus(),
                device.getIpAddress(),
                device.getFirmwareVersion(),
                device.getLastSeenAt(),
                device.getCreatedAt(),
                device.getUpdatedAt()
        );
    }
}
