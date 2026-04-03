package com.slamonitor.ingestor.infrastructure.persistence;

import com.slamonitor.ingestor.domain.model.MonitoredService;
import com.slamonitor.ingestor.domain.port.MonitoredServiceRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public class MonitoredServiceJpaAdapter implements MonitoredServiceRepository {

    private final SpringMonitoredServiceJpaRepository jpa;

    public MonitoredServiceJpaAdapter(SpringMonitoredServiceJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Optional<MonitoredService> findById(UUID id) {
        return jpa.findById(id);
    }
}
