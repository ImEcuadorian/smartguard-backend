package io.github.imecuadorian.smartguardbackend.actuator.api;

import io.github.imecuadorian.smartguardbackend.actuator.application.ActuatorService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/actuators")
public class ActuatorController {

    private final ActuatorService actuatorService;

    public ActuatorController(ActuatorService actuatorService) {
        this.actuatorService = actuatorService;
    }

    @PostMapping
    public ResponseEntity<ActuatorResponse> create(@Valid @RequestBody CreateActuatorRequest request) {
        var response = actuatorService.create(request);
        return ResponseEntity.created(URI.create("/api/v1/actuators/" + response.id())).body(response);
    }

    @GetMapping
    public ResponseEntity<List<ActuatorResponse>> findAll() {
        return ResponseEntity.ok(actuatorService.findAll());
    }

    @PostMapping("/{id}/commands")
    public ResponseEntity<ActuatorCommandResponse> createCommand(@PathVariable UUID id,
                                                                 @Valid @RequestBody CreateActuatorCommandRequest request) {
        var response = actuatorService.createCommand(id, request);
        return ResponseEntity.created(URI.create("/api/v1/actuators/" + id + "/commands/" + response.id()))
                .body(response);
    }

    @GetMapping("/{id}/commands")
    public ResponseEntity<List<ActuatorCommandResponse>> findCommands(@PathVariable UUID id) {
        return ResponseEntity.ok(actuatorService.findCommands(id));
    }
}
