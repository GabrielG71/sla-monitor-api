package com.slamonitor.processor.domain.model;

import java.time.Instant;
import java.util.UUID;

public record SlaViolation(
        UUID ruleId,
        UUID endpointId,
        String ruleType,
        String severity,
        Instant triggeredAt,
        String detail
) {
    public static SlaViolation from(SlaRule rule, CheckResult check, int consecutiveFailures) {
        return new SlaViolation(
                rule.getId(),
                rule.getEndpointId(),
                rule.getRuleType().name(),
                rule.getSeverity().name(),
                check.checkedAt(),
                consecutiveFailures + " consecutive failures (threshold: "
                        + rule.getThresholdValue().intValue() + ")"
        );
    }
}
