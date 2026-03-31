-- V1__create_services.sql
-- Registered clients/tenants that own endpoints

CREATE TABLE services (
    id         UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    name       VARCHAR(100) NOT NULL UNIQUE,
    api_key    VARCHAR(255) NOT NULL,   -- bcrypt hashed
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    active     BOOLEAN     NOT NULL DEFAULT true
);
