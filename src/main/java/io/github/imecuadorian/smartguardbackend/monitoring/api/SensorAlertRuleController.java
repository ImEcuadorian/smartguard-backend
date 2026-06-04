package io.github.imecuadorian.smartguardbackend.monitoring.api;

import io.github.imecuadorian.smartguardbackend.monitoring.application.SensorAlertRuleService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
public class SensorAlertRuleController {

    private final SensorAlertRuleService ruleService;

    public SensorAlertRuleController(SensorAlertRuleService ruleService) {
        this.ruleService = ruleService;
    }

    @PostMapping("/sensors/{sensorId}/alert-rules")
    public ResponseEntity<SensorAlertRuleResponse> create(@PathVariable UUID sensorId,
                                                          @Valid @RequestBody CreateSensorAlertRuleRequest request) {
        var response = ruleService.create(sensorId, request);
        return ResponseEntity.created(URI.create("/api/v1/sensor-alert-rules/" + response.id())).body(response);
    }

    @GetMapping("/sensors/{sensorId}/alert-rules")
    public ResponseEntity<List<SensorAlertRuleResponse>> findBySensor(@PathVariable UUID sensorId) {
        return ResponseEntity.ok(ruleService.findBySensor(sensorId));
    }

    @PatchMapping("/sensor-alert-rules/{id}")
    public ResponseEntity<SensorAlertRuleResponse> update(@PathVariable UUID id,
                                                          @Valid @RequestBody UpdateSensorAlertRuleRequest request) {
        return ResponseEntity.ok(ruleService.update(id, request));
    }

    @PatchMapping("/sensor-alert-rules/{id}/disable")
    public ResponseEntity<SensorAlertRuleResponse> disable(@PathVariable UUID id) {
        return ResponseEntity.ok(ruleService.disable(id));
    }
}
