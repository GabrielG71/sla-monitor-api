package com.slamonitor.ingestor.application.usecase;

import com.slamonitor.ingestor.domain.exception.EndpointNotFoundException;
import com.slamonitor.ingestor.domain.port.EndpointRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Component
public class DeactivateEndpointUseCase {

    private final EndpointRepository endpointRepository;

    public DeactivateEndpointUseCase(EndpointRepository endpointRepository) {
        this.endpointRepository = endpointRepository;
    }

    @Transactional
    public void execute(UUID id) {
        var endpoint = endpointRepository.findById(id)
                .filter(e -> e.isActive())
                .orElseThrow(() -> new EndpointNotFoundException(id));
        endpoint.deactivate();
        endpointRepository.save(endpoint);
    }
}
