package com.slamonitor.ingestor.domain.model;

import java.time.Instant;
import java.util.UUID;

public record CheckResult(
        UUID endpointId,
        Instant checkedAt,
        Integer statusCode,
        Integer latencyMs,
        boolean success,
        String errorDetail
) {}
