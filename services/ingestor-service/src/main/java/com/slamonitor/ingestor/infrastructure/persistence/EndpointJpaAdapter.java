package com.slamonitor.ingestor.infrastructure.persistence;

import com.slamonitor.ingestor.domain.model.Endpoint;
import com.slamonitor.ingestor.domain.port.EndpointRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class EndpointJpaAdapter implements EndpointRepository {

    private final SpringEndpointJpaRepository jpa;

    public EndpointJpaAdapter(SpringEndpointJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Endpoint save(Endpoint endpoint) {
        return jpa.save(endpoint);
    }

    @Override
    public Optional<Endpoint> findById(UUID id) {
        return jpa.findById(id);
    }

    @Override
    public List<Endpoint> findAllActive() {
        return jpa.findByActiveTrue();
    }
}
