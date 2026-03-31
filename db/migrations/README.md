# Database Migrations

Flyway migrations live **inside each service** under `src/main/resources/db/migration/`.

Only **`ingestor-service`** runs migrations (`spring.flyway.enabled=true`).  
`sla-processor` and `alert-service` have Flyway disabled — they rely on the schema already being in place.

## Migration ownership

| File | Owner service | Tables created |
|------|--------------|----------------|
| V1__create_services.sql | ingestor-service | `services` |
| V2__create_endpoints.sql | ingestor-service | `endpoints` |
| V3__create_sla_rules.sql | ingestor-service | `sla_rules` |
| V4__create_checks.sql | ingestor-service | `checks` |
| V5__create_alerts.sql | ingestor-service | `alerts` |

## Convention

- One concern per file — never alter existing migrations, only add new versioned files.
- Sequential versioning: V1, V2, V3...
- Files live at: `services/ingestor-service/src/main/resources/db/migration/`
