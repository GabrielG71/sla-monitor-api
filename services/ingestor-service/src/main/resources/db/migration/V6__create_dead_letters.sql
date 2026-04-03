-- V6__create_dead_letters.sql
-- Dead Letter Topic inspection table — written by sla-processor's DLT consumer.
CREATE TABLE dead_letters (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    topic       VARCHAR(100)  NOT NULL,
    partition_n INT           NOT NULL,
    offset_n    BIGINT        NOT NULL,
    failed_at   TIMESTAMPTZ   NOT NULL DEFAULT now(),
    error_class VARCHAR(255),
    error_msg   TEXT,
    payload     TEXT
);

CREATE INDEX idx_dead_letters_failed ON dead_letters(failed_at DESC);
