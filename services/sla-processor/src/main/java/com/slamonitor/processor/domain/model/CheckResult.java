package com.slamonitor.processor.domain.model;

import java.time.Instant;
import java.util.UUID;

/**
 * Mirrors the CheckResult published by ingestor-service to raw-checks.
 * Field names must match the JSON payload exactly (no type headers).
 */
public record CheckResult(
        UUID endpointId,
        Instant checkedAt,
        Integer statusCode,
        Integer latencyMs,
        boolean success,
        String errorDetail
) {}
