package com.slamonitor.ingestor.application.usecase;

import com.slamonitor.ingestor.domain.exception.EndpointNotFoundException;
import com.slamonitor.ingestor.domain.model.Endpoint;
import com.slamonitor.ingestor.domain.port.EndpointRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

@Component
public class UpdateEndpointUseCase {

    private final EndpointRepository endpointRepository;

    public UpdateEndpointUseCase(EndpointRepository endpointRepository) {
        this.endpointRepository = endpointRepository;
    }

    @Transactional
    public Endpoint execute(UUID id, String url, String httpMethod,
                            Map<String, String> headers, int timeoutMs, int intervalSecs) {
        var endpoint = endpointRepository.findById(id)
                .filter(Endpoint::isActive)
                .orElseThrow(() -> new EndpointNotFoundException(id));
        endpoint.update(url, httpMethod, headers, timeoutMs, intervalSecs);
        return endpointRepository.save(endpoint);
    }
}
