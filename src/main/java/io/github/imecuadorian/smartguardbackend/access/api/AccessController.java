package io.github.imecuadorian.smartguardbackend.access.api;

import io.github.imecuadorian.smartguardbackend.access.application.AccessService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/v1/access")
public class AccessController {

    private final AccessService accessService;

    public AccessController(AccessService accessService) {
        this.accessService = accessService;
    }

    @PostMapping("/readers")
    public ResponseEntity<AccessReaderResponse> createReader(@Valid @RequestBody CreateAccessReaderRequest request) {
        var response = accessService.createReader(request);
        return ResponseEntity.created(URI.create("/api/v1/access/readers/" + response.id())).body(response);
    }

    @GetMapping("/readers")
    public ResponseEntity<List<AccessReaderResponse>> findReaders() {
        return ResponseEntity.ok(accessService.findReaders());
    }

    @PostMapping("/cards")
    public ResponseEntity<RfidCardResponse> createCard(@Valid @RequestBody CreateRfidCardRequest request) {
        var response = accessService.createCard(request);
        return ResponseEntity.created(URI.create("/api/v1/access/cards/" + response.id())).body(response);
    }

    @GetMapping("/cards")
    public ResponseEntity<List<RfidCardResponse>> findCards() {
        return ResponseEntity.ok(accessService.findCards());
    }

    @PostMapping("/events/scan")
    public ResponseEntity<AccessEventResponse> scan(@Valid @RequestBody AccessScanRequest request) {
        var response = accessService.scan(request);
        return ResponseEntity.created(URI.create("/api/v1/access/events/" + response.id())).body(response);
    }

    @GetMapping("/events")
    public ResponseEntity<List<AccessEventResponse>> findEvents(@RequestParam(required = false) Instant from,
                                                               @RequestParam(required = false) Instant to,
                                                               @RequestParam(required = false) Integer limit) {
        return ResponseEntity.ok(accessService.findEvents(from, to, limit));
    }
}
