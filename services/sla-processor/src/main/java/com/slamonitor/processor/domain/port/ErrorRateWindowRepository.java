package com.slamonitor.processor.domain.port;

import java.util.UUID;

public interface ErrorRateWindowRepository {
    void record(UUID endpointId, boolean success, int windowSeconds);
    double getErrorRate(UUID endpointId);
}
