package com.slamonitor.ingestor.infrastructure.web;

import com.slamonitor.ingestor.application.usecase.DeactivateEndpointUseCase;
import com.slamonitor.ingestor.application.usecase.RegisterEndpointUseCase;
import com.slamonitor.ingestor.application.usecase.UpdateEndpointUseCase;
import com.slamonitor.ingestor.domain.exception.EndpointNotFoundException;
import com.slamonitor.ingestor.domain.model.PollHealth;
import com.slamonitor.ingestor.domain.port.EndpointRepository;
import com.slamonitor.ingestor.domain.port.PollHealthRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/endpoints")
public class EndpointController {

    private final RegisterEndpointUseCase registerEndpoint;
    private final UpdateEndpointUseCase updateEndpoint;
    private final DeactivateEndpointUseCase deactivateEndpoint;
    private final EndpointRepository endpointRepository;
    private final PollHealthRepository pollHealthRepository;

    public EndpointController(RegisterEndpointUseCase registerEndpoint,
                              UpdateEndpointUseCase updateEndpoint,
                              DeactivateEndpointUseCase deactivateEndpoint,
                              EndpointRepository endpointRepository,
                              PollHealthRepository pollHealthRepository) {
        this.registerEndpoint = registerEndpoint;
        this.updateEndpoint = updateEndpoint;
        this.deactivateEndpoint = deactivateEndpoint;
        this.endpointRepository = endpointRepository;
        this.pollHealthRepository = pollHealthRepository;
    }

    @PostMapping
    public ResponseEntity<EndpointResponse> create(@Valid @RequestBody CreateEndpointRequest req) {
        var endpoint = registerEndpoint.execute(
                req.serviceId(), req.url(), req.httpMethod(),
                req.headers(), req.timeoutMs(), req.intervalSecs());
        return ResponseEntity.status(HttpStatus.CREATED).body(EndpointResponse.from(endpoint));
    }

    @GetMapping
    public List<EndpointResponse> listActive() {
        return endpointRepository.findAllActive().stream()
                .map(EndpointResponse::from)
                .toList();
    }

    @GetMapping("/{id}")
    public EndpointResponse getById(@PathVariable UUID id) {
        return endpointRepository.findById(id)
                .map(EndpointResponse::from)
                .orElseThrow(() -> new EndpointNotFoundException(id));
    }

    @PutMapping("/{id}")
    public EndpointResponse update(@PathVariable UUID id,
                                   @Valid @RequestBody UpdateEndpointRequest req) {
        var endpoint = updateEndpoint.execute(
                id, req.url(), req.httpMethod(),
                req.headers(), req.timeoutMs(), req.intervalSecs());
        return EndpointResponse.from(endpoint);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deactivate(@PathVariable UUID id) {
        deactivateEndpoint.execute(id);
    }

    @GetMapping("/{id}/health")
    public ResponseEntity<PollHealth> health(@PathVariable UUID id) {
        return pollHealthRepository.getLatest(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }
}
