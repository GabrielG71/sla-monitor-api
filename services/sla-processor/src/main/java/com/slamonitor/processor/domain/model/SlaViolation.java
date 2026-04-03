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
    public static SlaViolation forAvailability(SlaRule rule, CheckResult check, int consecutiveFailures) {
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

    public static SlaViolation forLatency(SlaRule rule, CheckResult check, double p95) {
        return new SlaViolation(
                rule.getId(),
                rule.getEndpointId(),
                rule.getRuleType().name(),
                rule.getSeverity().name(),
                check.checkedAt(),
                "p95 latency " + (long) p95 + "ms exceeds threshold "
                        + rule.getThresholdValue().longValue() + "ms"
        );
    }

    public static SlaViolation forErrorRate(SlaRule rule, CheckResult check, double errorRate) {
        return new SlaViolation(
                rule.getId(),
                rule.getEndpointId(),
                rule.getRuleType().name(),
                rule.getSeverity().name(),
                check.checkedAt(),
                String.format("error rate %.1f%% exceeds threshold %s%%",
                        errorRate, rule.getThresholdValue().toPlainString())
        );
    }
}
