package com.slamonitor.alert.domain.model;

import java.time.Instant;
import java.util.List;

public record SlaReport(String month, Instant generatedAt, List<EndpointSlaReport> endpoints) {}
