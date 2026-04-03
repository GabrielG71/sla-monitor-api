package com.slamonitor.alert.infrastructure.web;

import com.slamonitor.alert.domain.model.Alert;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

public record IncidentResponse(
        UUID id,
        UUID endpointId,
        UUID slaRuleId,
        String ruleType,
        String severity,
        String status,
        Instant triggeredAt,
        Instant acknowledgedAt,
        Instant resolvedAt,
        Long durationMinutes
) {
    public static IncidentResponse from(Alert a) {
        Long duration = a.getResolvedAt() != null
                ? Duration.between(a.getTriggeredAt(), a.getResolvedAt()).toMinutes()
                : null;
        String ruleType = a.getMetadata() != null ? a.getMetadata().get("ruleType") : null;
        return new IncidentResponse(
                a.getId(), a.getEndpointId(), a.getSlaRuleId(),
                ruleType, a.getSeverity().name(), a.getStatus().name(),
                a.getTriggeredAt(), a.getAcknowledgedAt(), a.getResolvedAt(),
                duration);
    }
}
