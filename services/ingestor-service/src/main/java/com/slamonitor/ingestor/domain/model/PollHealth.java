package com.slamonitor.ingestor.domain.model;

import java.time.Instant;
import java.util.UUID;

public record PollHealth(
        UUID endpointId,
        Instant checkedAt,
        boolean success,
        Integer statusCode,
        Integer latencyMs
) {}
