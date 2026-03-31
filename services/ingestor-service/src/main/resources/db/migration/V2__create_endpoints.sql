-- V2__create_endpoints.sql
-- Endpoints to be polled, owned by a service

CREATE TABLE endpoints (
    id             UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    service_id     UUID         NOT NULL REFERENCES services(id),
    url            TEXT         NOT NULL,
    http_method    VARCHAR(10)  NOT NULL DEFAULT 'GET',
    headers        JSONB,
    timeout_ms     INT          NOT NULL DEFAULT 5000,
    interval_secs  INT          NOT NULL DEFAULT 60,
    active         BOOLEAN      NOT NULL DEFAULT true,
    created_at     TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE INDEX idx_endpoints_service ON endpoints(service_id);
CREATE INDEX idx_endpoints_active   ON endpoints(active) WHERE active = true;
