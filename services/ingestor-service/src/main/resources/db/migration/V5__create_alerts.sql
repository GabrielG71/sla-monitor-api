-- V5__create_alerts.sql
-- Alerts raised by alert-service with state machine: OPEN → ACKNOWLEDGED → RESOLVED

CREATE TABLE alerts (
    id               UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    endpoint_id      UUID        NOT NULL REFERENCES endpoints(id),
    sla_rule_id      UUID        NOT NULL REFERENCES sla_rules(id),
    status           VARCHAR(20) NOT NULL DEFAULT 'OPEN',  -- OPEN | ACKNOWLEDGED | RESOLVED
    severity         VARCHAR(20) NOT NULL,
    triggered_at     TIMESTAMPTZ NOT NULL DEFAULT now(),
    acknowledged_at  TIMESTAMPTZ,
    resolved_at      TIMESTAMPTZ,
    metadata         JSONB
);

CREATE INDEX idx_alerts_endpoint_status ON alerts(endpoint_id, status);
CREATE INDEX idx_alerts_triggered        ON alerts(triggered_at DESC);
