package com.slamonitor.alert.domain.model;

import java.time.Instant;
import java.util.UUID;

/**
 * Mirrors the SlaViolation published by sla-processor to sla-violations.
 * Field names must match the JSON payload exactly (no type headers).
 */
public record SlaViolation(
        UUID ruleId,
        UUID endpointId,
        String ruleType,
        String severity,
        Instant triggeredAt,
        String detail,
        int windowSeconds
) {}
