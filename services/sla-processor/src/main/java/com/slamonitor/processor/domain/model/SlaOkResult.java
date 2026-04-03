package com.slamonitor.processor.domain.model;

import java.time.Instant;
import java.util.UUID;

public record SlaOkResult(
        UUID endpointId,
        UUID ruleId,
        Instant evaluatedAt
) {}
