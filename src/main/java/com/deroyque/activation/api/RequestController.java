package com.deroyque.activation.api;
import com.deroyque.activation.service.*;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import java.net.URI;
import java.util.UUID;

@RestController @RequestMapping("/api/v1/requests")
public class RequestController {
    private final ActivationOrchestrator orchestrator; private final ActivationRequestService service;
    public RequestController(ActivationOrchestrator orchestrator, ActivationRequestService service) { this.orchestrator = orchestrator; this.service = service; }
    @PostMapping
    public ResponseEntity<RequestResponse> create(@Valid @RequestBody CreateRequest input) {
        var result = orchestrator.createAndActivate(input); var response = RequestResponse.from(result.request());
        if (!result.created()) return ResponseEntity.ok(response);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(response.id()).toUri();
        return ResponseEntity.created(location).body(response);
    }
    @GetMapping("/{id}") public RequestResponse find(@PathVariable UUID id) { return RequestResponse.from(service.find(id)); }
}
