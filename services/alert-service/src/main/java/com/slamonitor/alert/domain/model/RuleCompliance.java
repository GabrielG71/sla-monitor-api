package com.slamonitor.alert.domain.model;

import java.math.BigDecimal;
import java.util.UUID;

public record RuleCompliance(
        UUID ruleId,
        String ruleType,
        BigDecimal slaTarget,
        BigDecimal thresholdValue,
        String thresholdUnit,
        double measuredValue,
        String measuredUnit,
        boolean compliant,
        long incidentCount,
        long downtimeMinutes
) {}
