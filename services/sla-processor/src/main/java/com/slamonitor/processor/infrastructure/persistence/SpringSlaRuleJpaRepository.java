package com.slamonitor.processor.infrastructure.persistence;

import com.slamonitor.processor.domain.model.SlaRule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

interface SpringSlaRuleJpaRepository extends JpaRepository<SlaRule, UUID> {
    List<SlaRule> findByEndpointIdAndEnabledTrue(UUID endpointId);
}
