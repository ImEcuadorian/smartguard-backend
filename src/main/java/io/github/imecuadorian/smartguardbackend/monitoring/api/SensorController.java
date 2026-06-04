package io.github.imecuadorian.smartguardbackend.monitoring.api;

import io.github.imecuadorian.smartguardbackend.monitoring.application.SensorService;
import io.github.imecuadorian.smartguardbackend.monitoring.domain.SensorStatus;
import io.github.imecuadorian.smartguardbackend.monitoring.domain.SensorType;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/sensors")
public class SensorController {

    private final SensorService sensorService;

    public SensorController(SensorService sensorService) {
        this.sensorService = sensorService;
    }

    @PostMapping
    public ResponseEntity<SensorResponse> create(@Valid @RequestBody CreateSensorRequest request) {
        var response = sensorService.create(request);
        return ResponseEntity.created(URI.create("/api/v1/sensors/" + response.id())).body(response);
    }

    @GetMapping
    public ResponseEntity<List<SensorResponse>> findAll(@RequestParam(required = false) UUID deviceId,
                                                        @RequestParam(required = false) SensorStatus status,
                                                        @RequestParam(required = false) SensorType type) {
        return ResponseEntity.ok(sensorService.findAll(deviceId, status, type));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SensorResponse> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(sensorService.findById(id));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<SensorResponse> updateStatus(@PathVariable UUID id,
                                                       @Valid @RequestBody UpdateSensorStatusRequest request) {
        return ResponseEntity.ok(sensorService.updateStatus(id, request));
    }

    @PostMapping("/{id}/readings")
    public ResponseEntity<SensorReadingResponse> createReading(@PathVariable UUID id,
                                                               @Valid @RequestBody CreateSensorReadingRequest request) {
        var response = sensorService.createReading(id, request);
        return ResponseEntity.created(URI.create("/api/v1/sensors/" + id + "/readings/" + response.id()))
                .body(response);
    }

    @GetMapping("/{id}/readings")
    public ResponseEntity<List<SensorReadingResponse>> findReadings(@PathVariable UUID id,
                                                                    @RequestParam(required = false) Instant from,
                                                                    @RequestParam(required = false) Instant to,
                                                                    @RequestParam(required = false) Integer limit) {
        return ResponseEntity.ok(sensorService.findReadings(id, from, to, limit));
    }

    @GetMapping("/{id}/readings/latest")
    public ResponseEntity<SensorReadingResponse> findLatestReading(@PathVariable UUID id) {
        return ResponseEntity.ok(sensorService.findLatestReading(id));
    }
}
