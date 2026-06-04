package io.github.imecuadorian.smartguardbackend.device.api;

import io.github.imecuadorian.smartguardbackend.device.application.DeviceService;
import io.github.imecuadorian.smartguardbackend.device.domain.DeviceStatus;
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
@RequestMapping("/api/v1/devices")
public class DeviceController {

    private final DeviceService deviceService;

    public DeviceController(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @PostMapping
    public ResponseEntity<DeviceRegistrationResponse> create(@Valid @RequestBody CreateDeviceRequest request) {
        var response = deviceService.create(request);
        return ResponseEntity.created(URI.create("/api/v1/devices/" + response.device().id())).body(response);
    }

    @GetMapping
    public ResponseEntity<List<DeviceResponse>> findAll(@RequestParam(required = false) DeviceStatus status) {
        return ResponseEntity.ok(deviceService.findAll(status));
    }

    @GetMapping("/{id}")
    public ResponseEntity<DeviceResponse> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(deviceService.findById(id));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<DeviceResponse> update(@PathVariable UUID id, @Valid @RequestBody UpdateDeviceRequest request) {
        return ResponseEntity.ok(deviceService.update(id, request));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<DeviceResponse> updateStatus(@PathVariable UUID id,
                                                       @Valid @RequestBody UpdateDeviceStatusRequest request) {
        return ResponseEntity.ok(deviceService.updateStatus(id, request));
    }
}
