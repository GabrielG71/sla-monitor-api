-- V3__create_sla_rules.sql
-- SLA rules evaluated per endpoint

CREATE TABLE sla_rules (
    id               UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    endpoint_id      UUID        NOT NULL REFERENCES endpoints(id),
    rule_type        VARCHAR(30) NOT NULL,    -- AVAILABILITY | LATENCY | ERROR_RATE
    threshold_value  NUMERIC     NOT NULL,
    threshold_unit   VARCHAR(20) NOT NULL,    -- PERCENT | MS
    window_seconds   INT         NOT NULL,
    severity         VARCHAR(20) NOT NULL,    -- CRITICAL | WARNING | INFO
    sla_target       NUMERIC,                 -- contractual SLA % (e.g. 99.9)
    enabled          BOOLEAN     NOT NULL DEFAULT true
);

CREATE INDEX idx_sla_rules_endpoint ON sla_rules(endpoint_id);
