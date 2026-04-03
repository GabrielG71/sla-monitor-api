package com.slamonitor.ingestor.infrastructure.persistence;

import com.slamonitor.ingestor.domain.model.MonitoredService;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

interface SpringMonitoredServiceJpaRepository extends JpaRepository<MonitoredService, UUID> {
}
