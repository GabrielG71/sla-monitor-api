package com.slamonitor.processor.domain.port;

import com.slamonitor.processor.domain.model.SlaRule;
import java.util.List;
import java.util.UUID;

public interface SlaRuleRepository {
    List<SlaRule> findActiveByEndpointId(UUID endpointId);
}
