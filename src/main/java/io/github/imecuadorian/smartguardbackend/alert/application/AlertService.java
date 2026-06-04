package io.github.imecuadorian.smartguardbackend.alert.application;

import io.github.imecuadorian.smartguardbackend.alert.api.AlertMapper;
import io.github.imecuadorian.smartguardbackend.alert.api.AlertResponse;
import io.github.imecuadorian.smartguardbackend.alert.api.CreateAlertRequest;
import io.github.imecuadorian.smartguardbackend.alert.domain.Alert;
import io.github.imecuadorian.smartguardbackend.alert.domain.AlertSeverity;
import io.github.imecuadorian.smartguardbackend.alert.domain.AlertStatus;
import io.github.imecuadorian.smartguardbackend.alert.domain.AlertType;
import io.github.imecuadorian.smartguardbackend.alert.infrastructure.AlertRepository;
import io.github.imecuadorian.smartguardbackend.device.domain.Device;
import io.github.imecuadorian.smartguardbackend.device.infrastructure.DeviceRepository;
import io.github.imecuadorian.smartguardbackend.monitoring.domain.Sensor;
import io.github.imecuadorian.smartguardbackend.monitoring.infrastructure.SensorRepository;
import io.github.imecuadorian.smartguardbackend.realtime.application.RealtimeNotifier;
import io.github.imecuadorian.smartguardbackend.shared.error.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class AlertService {

    private final DeviceRepository deviceRepository;
    private final SensorRepository sensorRepository;
    private final AlertRepository alertRepository;
    private final AlertMapper alertMapper;
    private final RealtimeNotifier realtimeNotifier;

    public AlertService(DeviceRepository deviceRepository, SensorRepository sensorRepository,
                        AlertRepository alertRepository, AlertMapper alertMapper) {
        this(deviceRepository, sensorRepository, alertRepository, alertMapper, RealtimeNotifier.noop());
    }

    @Autowired
    public AlertService(DeviceRepository deviceRepository, SensorRepository sensorRepository,
                        AlertRepository alertRepository, AlertMapper alertMapper, RealtimeNotifier realtimeNotifier) {
        this.deviceRepository = deviceRepository;
        this.sensorRepository = sensorRepository;
        this.alertRepository = alertRepository;
        this.alertMapper = alertMapper;
        this.realtimeNotifier = realtimeNotifier;
    }

    public AlertResponse create(CreateAlertRequest request) {
        Device device = null;
        Sensor sensor = null;

        if (request.deviceId() != null) {
            device = deviceRepository.findById(request.deviceId())
                    .orElseThrow(() -> new ResourceNotFoundException("Device not found"));
        }

        if (request.sensorId() != null) {
            sensor = sensorRepository.findById(request.sensorId())
                    .orElseThrow(() -> new ResourceNotFoundException("Sensor not found"));
            device = sensor.getDevice();
        }

        Alert alert = sensor == null
                ? Alert.withoutSensor(device, request.type(), request.severity(), request.message(), request.occurredAt())
                : Alert.withSensor(device, sensor, request.type(), request.severity(), request.message(), request.occurredAt());

        var response = alertMapper.toResponse(alertRepository.save(alert));
        realtimeNotifier.alertChanged(response);
        return response;
    }

    public Optional<AlertResponse> createAutomaticAlertIfAbsent(Device device, Sensor sensor, AlertType type,
                                                                AlertSeverity severity, String message,
                                                                Instant occurredAt) {
        if (sensor != null && sensor.getId() != null && alertRepository.existsBySensorIdAndTypeAndStatusIn(
                sensor.getId(),
                type,
                List.of(AlertStatus.OPEN, AlertStatus.ACKNOWLEDGED)
        )) {
            return Optional.empty();
        }

        var alert = sensor == null
                ? Alert.withoutSensor(device, type, severity, message, occurredAt)
                : Alert.withSensor(device, sensor, type, severity, message, occurredAt);
        var response = alertMapper.toResponse(alertRepository.save(alert));
        realtimeNotifier.alertChanged(response);
        return Optional.of(response);
    }

    @Transactional(readOnly = true)
    public List<AlertResponse> findAll() {
        return findAll(null, null);
    }

    @Transactional(readOnly = true)
    public List<AlertResponse> findAll(AlertStatus status, AlertSeverity severity) {
        return alertRepository.findAllFiltered(status, severity).stream().map(alertMapper::toResponse).toList();
    }

    public AlertResponse acknowledge(UUID id) {
        var alert = findAlert(id);
        alert.acknowledge(Instant.now());
        var response = alertMapper.toResponse(alert);
        realtimeNotifier.alertChanged(response);
        return response;
    }

    public AlertResponse resolve(UUID id) {
        var alert = findAlert(id);
        alert.resolve(Instant.now());
        var response = alertMapper.toResponse(alert);
        realtimeNotifier.alertChanged(response);
        return response;
    }

    private Alert findAlert(UUID id) {
        return alertRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Alert not found"));
    }
}
