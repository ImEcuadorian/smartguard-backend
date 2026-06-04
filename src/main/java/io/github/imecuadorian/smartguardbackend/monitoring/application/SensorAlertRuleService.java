package io.github.imecuadorian.smartguardbackend.monitoring.application;

import io.github.imecuadorian.smartguardbackend.monitoring.api.CreateSensorAlertRuleRequest;
import io.github.imecuadorian.smartguardbackend.monitoring.api.SensorAlertRuleMapper;
import io.github.imecuadorian.smartguardbackend.monitoring.api.SensorAlertRuleResponse;
import io.github.imecuadorian.smartguardbackend.monitoring.api.UpdateSensorAlertRuleRequest;
import io.github.imecuadorian.smartguardbackend.monitoring.domain.SensorAlertRule;
import io.github.imecuadorian.smartguardbackend.monitoring.domain.SensorAlertRuleType;
import io.github.imecuadorian.smartguardbackend.monitoring.infrastructure.SensorAlertRuleRepository;
import io.github.imecuadorian.smartguardbackend.monitoring.infrastructure.SensorRepository;
import io.github.imecuadorian.smartguardbackend.shared.error.InvalidRequestException;
import io.github.imecuadorian.smartguardbackend.shared.error.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class SensorAlertRuleService {

    private final SensorRepository sensorRepository;
    private final SensorAlertRuleRepository ruleRepository;
    private final SensorAlertRuleMapper ruleMapper;

    public SensorAlertRuleService(SensorRepository sensorRepository, SensorAlertRuleRepository ruleRepository,
                                  SensorAlertRuleMapper ruleMapper) {
        this.sensorRepository = sensorRepository;
        this.ruleRepository = ruleRepository;
        this.ruleMapper = ruleMapper;
    }

    public SensorAlertRuleResponse create(UUID sensorId, CreateSensorAlertRuleRequest request) {
        var sensor = sensorRepository.findById(sensorId)
                .orElseThrow(() -> new ResourceNotFoundException("Sensor not found"));

        validateCreate(request);
        var rule = new SensorAlertRule(
                sensor,
                request.type(),
                request.operator(),
                request.thresholdValue(),
                request.expectedBooleanValue(),
                request.durationMinutes(),
                request.alertType(),
                request.severity(),
                request.message()
        );

        return ruleMapper.toResponse(ruleRepository.save(rule));
    }

    @Transactional(readOnly = true)
    public List<SensorAlertRuleResponse> findBySensor(UUID sensorId) {
        if (!sensorRepository.existsById(sensorId)) {
            throw new ResourceNotFoundException("Sensor not found");
        }

        return ruleRepository.findAllBySensorIdOrderByCreatedAtAsc(sensorId)
                .stream()
                .map(ruleMapper::toResponse)
                .toList();
    }

    public SensorAlertRuleResponse update(UUID id, UpdateSensorAlertRuleRequest request) {
        var rule = findRule(id);
        rule.update(
                request.operator(),
                request.thresholdValue(),
                request.expectedBooleanValue(),
                request.durationMinutes(),
                request.alertType(),
                request.severity(),
                request.message(),
                request.enabled()
        );
        validateRule(rule);
        return ruleMapper.toResponse(rule);
    }

    public SensorAlertRuleResponse disable(UUID id) {
        var rule = findRule(id);
        rule.disable();
        return ruleMapper.toResponse(rule);
    }

    private SensorAlertRule findRule(UUID id) {
        return ruleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sensor alert rule not found"));
    }

    private void validateCreate(CreateSensorAlertRuleRequest request) {
        validateFields(
                request.type(),
                request.operator(),
                request.thresholdValue(),
                request.expectedBooleanValue(),
                request.durationMinutes()
        );
    }

    private void validateRule(SensorAlertRule rule) {
        validateFields(
                rule.getType(),
                rule.getOperator(),
                rule.getThresholdValue(),
                rule.getExpectedBooleanValue(),
                rule.getDurationMinutes()
        );
    }

    private void validateFields(SensorAlertRuleType type, Object operator, Object thresholdValue,
                                Boolean expectedBooleanValue, Integer durationMinutes) {
        if (type == SensorAlertRuleType.NUMERIC_THRESHOLD && (operator == null || thresholdValue == null)) {
            throw new InvalidRequestException("Numeric threshold rules require operator and thresholdValue");
        }
        if (type == SensorAlertRuleType.BOOLEAN_MATCH && expectedBooleanValue == null) {
            throw new InvalidRequestException("Boolean match rules require expectedBooleanValue");
        }
        if (type == SensorAlertRuleType.DURATION_OPEN
                && (expectedBooleanValue == null || durationMinutes == null || durationMinutes < 1)) {
            throw new InvalidRequestException("Duration open rules require expectedBooleanValue and durationMinutes");
        }
        if (type == SensorAlertRuleType.NO_READING && (durationMinutes == null || durationMinutes < 1)) {
            throw new InvalidRequestException("No reading rules require durationMinutes");
        }
    }
}
