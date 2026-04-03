package com.slamonitor.alert.domain.model;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "alerts")
public class Alert {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(name = "endpoint_id", nullable = false)
    private UUID endpointId;

    @Column(name = "sla_rule_id", nullable = false)
    private UUID slaRuleId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AlertStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Severity severity;

    @Column(name = "triggered_at", nullable = false, updatable = false)
    private Instant triggeredAt;

    @Column(name = "acknowledged_at")
    private Instant acknowledgedAt;

    @Column(name = "resolved_at")
    private Instant resolvedAt;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, String> metadata;

    protected Alert() {}

    public static Alert open(SlaViolation violation) {
        var a = new Alert();
        a.endpointId = violation.endpointId();
        a.slaRuleId = violation.ruleId();
        a.status = AlertStatus.OPEN;
        a.severity = Severity.valueOf(violation.severity());
        a.triggeredAt = violation.triggeredAt();
        a.metadata = Map.of(
                "ruleType", violation.ruleType(),
                "detail", violation.detail()
        );
        return a;
    }

    public void acknowledge() {
        if (this.status != AlertStatus.OPEN) {
            throw new IllegalStateException("Only OPEN alerts can be acknowledged");
        }
        this.status = AlertStatus.ACKNOWLEDGED;
        this.acknowledgedAt = Instant.now();
    }

    public void resolve() {
        if (this.status == AlertStatus.RESOLVED) {
            throw new IllegalStateException("Alert is already resolved");
        }
        this.status = AlertStatus.RESOLVED;
        this.resolvedAt = Instant.now();
    }

    public UUID getId() { return id; }
    public UUID getEndpointId() { return endpointId; }
    public UUID getSlaRuleId() { return slaRuleId; }
    public AlertStatus getStatus() { return status; }
    public Severity getSeverity() { return severity; }
    public Instant getTriggeredAt() { return triggeredAt; }
    public Instant getAcknowledgedAt() { return acknowledgedAt; }
    public Instant getResolvedAt() { return resolvedAt; }
    public Map<String, String> getMetadata() { return metadata; }
}
