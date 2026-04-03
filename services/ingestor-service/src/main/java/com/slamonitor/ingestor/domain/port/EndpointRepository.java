package com.slamonitor.ingestor.domain.port;

import com.slamonitor.ingestor.domain.model.Endpoint;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EndpointRepository {
    Endpoint save(Endpoint endpoint);
    Optional<Endpoint> findById(UUID id);
    List<Endpoint> findAllActive();
}
