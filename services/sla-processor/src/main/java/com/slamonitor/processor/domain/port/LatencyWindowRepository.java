package com.slamonitor.processor.domain.port;

import java.util.UUID;

public interface LatencyWindowRepository {
    void record(UUID endpointId, int latencyMs, int windowSeconds);
    double getPercentile(UUID endpointId, double percentile);
}
