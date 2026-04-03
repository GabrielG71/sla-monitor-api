package com.slamonitor.processor.infrastructure.persistence;

import com.slamonitor.processor.domain.model.SlaRule;
import com.slamonitor.processor.domain.port.SlaRuleRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public class SlaRuleJpaAdapter implements SlaRuleRepository {

    private final SpringSlaRuleJpaRepository jpa;

    public SlaRuleJpaAdapter(SpringSlaRuleJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    @Cacheable("sla-rules")
    public List<SlaRule> findActiveByEndpointId(UUID endpointId) {
        return jpa.findByEndpointIdAndEnabledTrue(endpointId);
    }
}
