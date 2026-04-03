package com.slamonitor.alert.domain.model;

import java.util.UUID;

public record EndpointInfo(UUID id, String url, String httpMethod) {}
