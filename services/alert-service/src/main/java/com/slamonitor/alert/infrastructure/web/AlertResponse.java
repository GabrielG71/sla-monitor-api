package com.slamonitor.alert.infrastructure.web;

import com.slamonitor.alert.domain.model.Alert;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record AlertResponse(
        UUID id,
        UUID endpointId,
        UUID slaRuleId,
        String status,
        String severity,
        Instant triggeredAt,
        Instant acknowledgedAt,
        Instant resolvedAt,
        Map<String, String> metadata
) {
    public static AlertResponse from(Alert a) {
        return new AlertResponse(
                a.getId(),
                a.getEndpointId(),
                a.getSlaRuleId(),
                a.getStatus().name(),
                a.getSeverity().name(),
                a.getTriggeredAt(),
                a.getAcknowledgedAt(),
                a.getResolvedAt(),
                a.getMetadata()
        );
    }
}
