-- V4__create_checks.sql
-- Raw poll results published by ingestor and persisted by sla-processor

CREATE TABLE checks (
    id           UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    endpoint_id  UUID        NOT NULL REFERENCES endpoints(id),
    checked_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
    status_code  INT,
    latency_ms   INT,
    success      BOOLEAN     NOT NULL,
    error_detail TEXT
);

CREATE INDEX idx_checks_endpoint_time ON checks(endpoint_id, checked_at DESC);
