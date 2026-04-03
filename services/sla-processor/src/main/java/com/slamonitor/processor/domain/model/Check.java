package com.slamonitor.processor.domain.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "checks")
public class Check {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(name = "endpoint_id", nullable = false)
    private UUID endpointId;

    @Column(name = "checked_at", nullable = false)
    private Instant checkedAt;

    @Column(name = "status_code")
    private Integer statusCode;

    @Column(name = "latency_ms")
    private Integer latencyMs;

    @Column(nullable = false)
    private boolean success;

    @Column(name = "error_detail")
    private String errorDetail;

    protected Check() {}

    public static Check from(CheckResult r) {
        var c = new Check();
        c.endpointId = r.endpointId();
        c.checkedAt = r.checkedAt();
        c.statusCode = r.statusCode();
        c.latencyMs = r.latencyMs();
        c.success = r.success();
        c.errorDetail = r.errorDetail();
        return c;
    }
}
