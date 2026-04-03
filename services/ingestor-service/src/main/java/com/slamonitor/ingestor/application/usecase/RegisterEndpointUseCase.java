package com.slamonitor.ingestor.application.usecase;

import com.slamonitor.ingestor.domain.exception.ServiceNotFoundException;
import com.slamonitor.ingestor.domain.model.Endpoint;
import com.slamonitor.ingestor.domain.port.EndpointRepository;
import com.slamonitor.ingestor.domain.port.MonitoredServiceRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

@Component
public class RegisterEndpointUseCase {

    private final EndpointRepository endpointRepository;
    private final MonitoredServiceRepository serviceRepository;

    public RegisterEndpointUseCase(EndpointRepository endpointRepository,
                                   MonitoredServiceRepository serviceRepository) {
        this.endpointRepository = endpointRepository;
        this.serviceRepository = serviceRepository;
    }

    @Transactional
    public Endpoint execute(UUID serviceId, String url, String httpMethod,
                            Map<String, String> headers, int timeoutMs, int intervalSecs) {
        var service = serviceRepository.findById(serviceId)
                .orElseThrow(() -> new ServiceNotFoundException(serviceId));
        var endpoint = Endpoint.create(service, url, httpMethod, headers, timeoutMs, intervalSecs);
        return endpointRepository.save(endpoint);
    }
}
