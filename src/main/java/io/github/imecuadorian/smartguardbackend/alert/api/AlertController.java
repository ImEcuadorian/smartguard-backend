package io.github.imecuadorian.smartguardbackend.alert.api;

import io.github.imecuadorian.smartguardbackend.alert.application.AlertService;
import io.github.imecuadorian.smartguardbackend.alert.domain.AlertSeverity;
import io.github.imecuadorian.smartguardbackend.alert.domain.AlertStatus;
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
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/alerts")
public class AlertController {

    private final AlertService alertService;

    public AlertController(AlertService alertService) {
        this.alertService = alertService;
    }

    @PostMapping
    public ResponseEntity<AlertResponse> create(@Valid @RequestBody CreateAlertRequest request) {
        var response = alertService.create(request);
        return ResponseEntity.created(URI.create("/api/v1/alerts/" + response.id())).body(response);
    }

    @GetMapping
    public ResponseEntity<List<AlertResponse>> findAll(@RequestParam(required = false) AlertStatus status,
                                                       @RequestParam(required = false) AlertSeverity severity) {
        return ResponseEntity.ok(alertService.findAll(status, severity));
    }

    @PatchMapping("/{id}/acknowledge")
    public ResponseEntity<AlertResponse> acknowledge(@PathVariable UUID id) {
        return ResponseEntity.ok(alertService.acknowledge(id));
    }

    @PatchMapping("/{id}/resolve")
    public ResponseEntity<AlertResponse> resolve(@PathVariable UUID id) {
        return ResponseEntity.ok(alertService.resolve(id));
    }
}
