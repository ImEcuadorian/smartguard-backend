package io.github.imecuadorian.smartguardbackend.monitoring.application;

import io.github.imecuadorian.smartguardbackend.device.domain.Device;
import io.github.imecuadorian.smartguardbackend.device.infrastructure.DeviceRepository;
import io.github.imecuadorian.smartguardbackend.monitoring.api.CreateSensorReadingRequest;
import io.github.imecuadorian.smartguardbackend.monitoring.api.CreateSensorRequest;
import io.github.imecuadorian.smartguardbackend.monitoring.api.SensorMapper;
import io.github.imecuadorian.smartguardbackend.monitoring.api.SensorReadingResponse;
import io.github.imecuadorian.smartguardbackend.monitoring.api.SensorResponse;
import io.github.imecuadorian.smartguardbackend.monitoring.api.UpdateSensorStatusRequest;
import io.github.imecuadorian.smartguardbackend.monitoring.domain.Sensor;
import io.github.imecuadorian.smartguardbackend.monitoring.domain.SensorReading;
import io.github.imecuadorian.smartguardbackend.monitoring.domain.SensorStatus;
import io.github.imecuadorian.smartguardbackend.monitoring.domain.SensorType;
import io.github.imecuadorian.smartguardbackend.monitoring.infrastructure.SensorReadingRepository;
import io.github.imecuadorian.smartguardbackend.monitoring.infrastructure.SensorRepository;
import io.github.imecuadorian.smartguardbackend.realtime.application.RealtimeNotifier;
import io.github.imecuadorian.smartguardbackend.shared.error.DuplicateResourceException;
import io.github.imecuadorian.smartguardbackend.shared.error.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class SensorService {

    private final DeviceRepository deviceRepository;
    private final SensorRepository sensorRepository;
    private final SensorReadingRepository readingRepository;
    private final SensorMapper sensorMapper;
    private final RealtimeNotifier realtimeNotifier;
    private final SensorAlertRuleEvaluator alertRuleEvaluator;

    public SensorService(DeviceRepository deviceRepository, SensorRepository sensorRepository,
                         SensorReadingRepository readingRepository, SensorMapper sensorMapper) {
        this(deviceRepository, sensorRepository, readingRepository, sensorMapper, RealtimeNotifier.noop(),
                SensorAlertRuleEvaluator.noop());
    }

    public SensorService(DeviceRepository deviceRepository, SensorRepository sensorRepository,
                         SensorReadingRepository readingRepository, SensorMapper sensorMapper,
                         SensorAlertRuleEvaluator alertRuleEvaluator) {
        this(deviceRepository, sensorRepository, readingRepository, sensorMapper, RealtimeNotifier.noop(),
                alertRuleEvaluator);
    }

    @Autowired
    public SensorService(DeviceRepository deviceRepository, SensorRepository sensorRepository,
                         SensorReadingRepository readingRepository, SensorMapper sensorMapper,
                         RealtimeNotifier realtimeNotifier, SensorAlertRuleEvaluator alertRuleEvaluator) {
        this.deviceRepository = deviceRepository;
        this.sensorRepository = sensorRepository;
        this.readingRepository = readingRepository;
        this.sensorMapper = sensorMapper;
        this.realtimeNotifier = realtimeNotifier;
        this.alertRuleEvaluator = alertRuleEvaluator;
    }

    public SensorResponse create(CreateSensorRequest request) {
        String code = request.code().trim();
        if (sensorRepository.existsByCode(code)) {
            throw new DuplicateResourceException("Sensor code already exists");
        }

        Device device = deviceRepository.findById(request.deviceId())
                .orElseThrow(() -> new ResourceNotFoundException("Device not found"));

        var sensor = new Sensor(device, code, request.name(), request.type(), request.unit(), request.location());
        return sensorMapper.toResponse(sensorRepository.save(sensor));
    }

    @Transactional(readOnly = true)
    public List<SensorResponse> findAll() {
        return findAll(null, null, null);
    }

    @Transactional(readOnly = true)
    public List<SensorResponse> findAll(UUID deviceId, SensorStatus status, SensorType type) {
        return sensorRepository.findAllFiltered(deviceId, status, type)
                .stream()
                .map(sensorMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public SensorResponse findById(UUID id) {
        return sensorMapper.toResponse(findSensor(id));
    }

    public SensorResponse updateStatus(UUID id, UpdateSensorStatusRequest request) {
        var sensor = findSensor(id);
        sensor.updateStatus(request.status());
        return sensorMapper.toResponse(sensor);
    }

    public SensorReadingResponse createReading(UUID sensorId, CreateSensorReadingRequest request) {
        var sensor = findSensor(sensorId);
        var reading = new SensorReading(
                sensor,
                request.numericValue(),
                request.booleanValue(),
                request.textValue(),
                request.recordedAt()
        );

        var savedReading = readingRepository.save(reading);
        sensor.markReadingAt(savedReading.getRecordedAt());
        alertRuleEvaluator.evaluate(savedReading);

        var response = sensorMapper.toResponse(savedReading);
        realtimeNotifier.sensorReadingCreated(response);
        return response;
    }

    public SensorReadingResponse createReadingByCode(String sensorCode, CreateSensorReadingRequest request) {
        var sensor = sensorRepository.findByCode(sensorCode.trim())
                .orElseThrow(() -> new ResourceNotFoundException("Sensor not found"));
        return createReading(sensor.getId(), request);
    }

    @Transactional(readOnly = true)
    public List<SensorReadingResponse> findReadings(UUID sensorId) {
        return findReadings(sensorId, null, null, null);
    }

    @Transactional(readOnly = true)
    public List<SensorReadingResponse> findReadings(UUID sensorId, Instant from, Instant to, Integer limit) {
        if (!sensorRepository.existsById(sensorId)) {
            throw new ResourceNotFoundException("Sensor not found");
        }

        int boundedLimit = limit == null ? 100 : Math.min(Math.max(limit, 1), 1000);
        return readingRepository.findReadings(sensorId, from, to, PageRequest.of(0, boundedLimit))
                .stream()
                .map(sensorMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public SensorReadingResponse findLatestReading(UUID sensorId) {
        if (!sensorRepository.existsById(sensorId)) {
            throw new ResourceNotFoundException("Sensor not found");
        }

        return readingRepository.findFirstBySensorIdOrderByRecordedAtDesc(sensorId)
                .map(sensorMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Sensor reading not found"));
    }

    private Sensor findSensor(UUID id) {
        return sensorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sensor not found"));
    }
}
