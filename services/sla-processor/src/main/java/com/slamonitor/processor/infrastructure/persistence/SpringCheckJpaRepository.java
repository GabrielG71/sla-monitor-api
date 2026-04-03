package com.slamonitor.processor.infrastructure.persistence;

import com.slamonitor.processor.domain.model.Check;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

interface SpringCheckJpaRepository extends JpaRepository<Check, UUID> {
}
