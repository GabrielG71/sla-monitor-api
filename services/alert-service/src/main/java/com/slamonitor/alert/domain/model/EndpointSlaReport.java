package com.slamonitor.alert.domain.model;

import java.util.List;
import java.util.UUID;

public record EndpointSlaReport(
        UUID endpointId,
        String url,
        long totalChecks,
        long successfulChecks,
        double availabilityPct,
        List<RuleCompliance> rules
) {}
