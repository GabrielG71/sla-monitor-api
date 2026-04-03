package com.slamonitor.ingestor.infrastructure.web;

import com.slamonitor.ingestor.domain.model.Endpoint;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record EndpointResponse(
        UUID id,
        UUID serviceId,
        String url,
        String httpMethod,
        Map<String, String> headers,
        int timeoutMs,
        int intervalSecs,
        boolean active,
        Instant createdAt
) {
    public static EndpointResponse from(Endpoint e) {
        return new EndpointResponse(
                e.getId(),
                e.getService().getId(),
                e.getUrl(),
                e.getHttpMethod(),
                e.getHeaders(),
                e.getTimeoutMs(),
                e.getIntervalSecs(),
                e.isActive(),
                e.getCreatedAt()
        );
    }
}
