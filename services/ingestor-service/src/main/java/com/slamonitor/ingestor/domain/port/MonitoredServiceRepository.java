package com.slamonitor.ingestor.domain.port;

import com.slamonitor.ingestor.domain.model.MonitoredService;
import java.util.Optional;
import java.util.UUID;

public interface MonitoredServiceRepository {
    Optional<MonitoredService> findById(UUID id);
}
