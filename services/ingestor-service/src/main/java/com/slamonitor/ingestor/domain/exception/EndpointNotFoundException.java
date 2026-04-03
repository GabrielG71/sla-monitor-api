package com.slamonitor.ingestor.domain.exception;

import java.util.UUID;

public class EndpointNotFoundException extends RuntimeException {

    public EndpointNotFoundException(UUID id) {
        super("Endpoint not found: " + id);
    }
}
