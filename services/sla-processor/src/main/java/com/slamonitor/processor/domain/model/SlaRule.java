package com.slamonitor.processor.domain.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "sla_rules")
public class SlaRule {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(name = "endpoint_id", nullable = false)
    private UUID endpointId;

    @Enumerated(EnumType.STRING)
    @Column(name = "rule_type", nullable = false, length = 30)
    private SlaRuleType ruleType;

    @Column(name = "threshold_value", nullable = false)
    private BigDecimal thresholdValue;

    @Column(name = "threshold_unit", nullable = false, length = 20)
    private String thresholdUnit;

    @Column(name = "window_seconds", nullable = false)
    private int windowSeconds;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Severity severity;

    @Column(name = "sla_target")
    private BigDecimal slaTarget;

    @Column(nullable = false)
    private boolean enabled;

    protected SlaRule() {}

    public UUID getId() { return id; }
    public UUID getEndpointId() { return endpointId; }
    public SlaRuleType getRuleType() { return ruleType; }
    public BigDecimal getThresholdValue() { return thresholdValue; }
    public String getThresholdUnit() { return thresholdUnit; }
    public int getWindowSeconds() { return windowSeconds; }
    public Severity getSeverity() { return severity; }
    public BigDecimal getSlaTarget() { return slaTarget; }
    public boolean isEnabled() { return enabled; }
}
