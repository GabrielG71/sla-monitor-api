package com.slamonitor.ingestor.infrastructure.persistence;

import com.slamonitor.ingestor.domain.model.Endpoint;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

interface SpringEndpointJpaRepository extends JpaRepository<Endpoint, UUID> {
    List<Endpoint> findByActiveTrue();
}
