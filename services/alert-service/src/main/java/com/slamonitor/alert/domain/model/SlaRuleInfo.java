package com.slamonitor.alert.domain.model;

import java.math.BigDecimal;
import java.util.UUID;

public record SlaRuleInfo(
        UUID id,
        UUID endpointId,
        String ruleType,
        BigDecimal thresholdValue,
        String thresholdUnit,
        int windowSeconds,
        BigDecimal slaTarget,
        String severity
) {}