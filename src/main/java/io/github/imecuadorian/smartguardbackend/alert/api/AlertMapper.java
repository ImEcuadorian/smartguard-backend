package io.github.imecuadorian.smartguardbackend.alert.api;

import io.github.imecuadorian.smartguardbackend.alert.domain.Alert;
import org.springframework.stereotype.Component;

@Component
public class AlertMapper {
    public AlertResponse toResponse(Alert alert) {
        return new AlertResponse(
                alert.getId(),
                alert.getDevice() == null ? null : alert.getDevice().getId(),
                alert.getSensor() == null ? null : alert.getSensor().getId(),
                alert.getType(),
                alert.getSeverity(),
                alert.getStatus(),
                alert.getMessage(),
                alert.getOccurredAt(),
                alert.getCreatedAt(),
                alert.getAcknowledgedAt(),
                alert.getResolvedAt()
        );
    }
}
