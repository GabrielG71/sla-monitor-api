package com.slamonitor.alert.domain.model;

import java.util.UUID;

public record CheckStats(
        UUID endpointId,
        long totalChecks,
        long successfulChecks,
        Double p95LatencyMs
) {
    public double availabilityPct() {
        return totalChecks == 0 ? 100.0 : (double) successfulChecks / totalChecks * 100.0;
    }

    public double errorRatePct() {
        return totalChecks == 0 ? 0.0 : (double) (totalChecks - successfulChecks) / totalChecks * 100.0;
    }
}