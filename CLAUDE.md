# CLAUDE.md — SLA Monitor

## Project Overview

**SLA Monitor** is a platform that actively monitors the availability and performance of third-party APIs that businesses depend on. It operates via scheduled HTTP polling — no agent or SDK required on the client side. The system collects real metrics per registered endpoint (latency, error rate, availability), processes configurable SLA rules through Kafka with a Spring Boot backend, stores history in PostgreSQL, uses Redis for caching and alert throttling, fires real-time notifications on violations, and generates SLA compliance reports — all exposed in a live Next.js dashboard.

---

## Monorepo Structure

```
sla-monitor/
├── services/
│   ├── ingestor-service/       # Polling scheduler + Kafka producer
│   ├── sla-processor/          # SLA rule evaluation (Kafka consumer)
│   └── alert-service/          # Alert dispatch + SSE endpoint
├── frontend/                   # Next.js dashboard
├── infra/
│   ├── docker-compose.yml
│   ├── docker-compose.override.yml   # dev overrides
│   └── kafka/
│       └── init-topics.sh
├── db/
│   └── migrations/             # Flyway SQL migrations (shared reference)
└── CLAUDE.md
```

---

## Tech Stack

### Backend
| Layer | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 3.x |
| Messaging | Apache Kafka (Confluent Platform 7.6) |
| Database | PostgreSQL 16 + Flyway |
| Cache / Throttling | Redis 7 |
| HTTP Client | Spring WebClient (reactive, non-blocking) |
| Build | Maven (per service) |

### Frontend
| Layer | Technology |
|---|---|
| Framework | Next.js 14 (App Router) |
| Language | TypeScript |
| Real-time | SSE (Server-Sent Events) |
| Styling | Tailwind CSS |

### Infrastructure
| Tool | Purpose |
|---|---|
| Docker + Compose | Local dev environment |
| Kafka UI | Topic inspection (dev only) |
| Zookeeper | Kafka coordination |

---

## Services

### 1. `ingestor-service`

**Responsibility**: Schedule HTTP polling for registered endpoints and publish raw check results to Kafka.

**Key behaviors:**
- Loads active endpoints from PostgreSQL on startup and caches in Redis
- Polls each endpoint on its configured interval using Spring `@Scheduled` + `WebClient`
- Measures wall-clock latency, HTTP status code, and response body (optional)
- Publishes to topic `raw-checks` partitioned by `endpointId`
- Deduplicates in-flight polls per endpoint via Redis lock (`SET NX EX`) to prevent overlap on slow responses
- Supports per-endpoint timeout configuration

**REST API (internal):**
```
POST   /endpoints          # Register new endpoint to monitor
PUT    /endpoints/{id}     # Update endpoint config
DELETE /endpoints/{id}     # Deactivate endpoint
POST   /endpoints/{id}/poll-now   # Manual trigger (dev/debug)
```

---

### 2. `sla-processor`

**Responsibility**: Consume raw check results, evaluate SLA rules, and publish violations.

**Key behaviors:**
- Consumes from `raw-checks` (consumer group: `sla-processors`)
- Loads SLA rules from PostgreSQL, cached locally via Caffeine with Redis Pub/Sub invalidation on rule update
- Evaluates rules per check result:
  - **AVAILABILITY** — endpoint down (non-2xx or timeout) for N consecutive checks
  - **LATENCY_P95** — rolling p95 latency over a time window exceeds threshold
  - **ERROR_RATE** — percentage of failed checks exceeds threshold over a window
- Publishes to `sla-ok` or `sla-violations` based on evaluation result
- Malformed or unprocessable events go to `raw-checks.DLT`
- Persists aggregated metrics to PostgreSQL for historical reporting

**SLA Rule types:**
```
AVAILABILITY   threshold_value: 99.9  threshold_unit: PERCENT  window_seconds: 3600
LATENCY        threshold_value: 500   threshold_unit: MS        window_seconds: 300
ERROR_RATE     threshold_value: 1.0   threshold_unit: PERCENT   window_seconds: 600
```

---

### 3. `alert-service`

**Responsibility**: Consume SLA violations, deduplicate, throttle, dispatch notifications, and stream live alerts to the frontend.

**Key behaviors:**
- Consumes from `sla-violations` (consumer group: `alert-dispatchers`)
- Throttling via Redis: suppresses duplicate alerts for the same rule within a configurable window
- Dispatches via: Webhook (HTTP POST), Email (SMTP), Slack (Incoming Webhook)
- Persists alerts to PostgreSQL with state machine: `OPEN → ACKNOWLEDGED → RESOLVED`
- Exposes SSE endpoint `GET /alerts/stream` for real-time frontend consumption
- Auto-resolves open alerts when subsequent checks pass

**REST API:**
```
GET    /alerts                  # List alerts (filterable by status, service, date)
GET    /alerts/{id}             # Alert detail
PATCH  /alerts/{id}/acknowledge # Acknowledge alert
PATCH  /alerts/{id}/resolve     # Manually resolve alert
GET    /alerts/stream           # SSE stream (real-time)
GET    /reports/sla             # Monthly SLA compliance report
```

---

## Kafka Topology

```
raw-checks           partitions: 6  key: endpointId
  └── sla-processor  (group: sla-processors)
        ├── sla-ok           partitions: 3
        └── sla-violations   partitions: 3
              └── alert-service (group: alert-dispatchers)

raw-checks.DLT       Dead Letter Topic — malformed events
```

**Partitioning rationale**: Keying by `endpointId` ensures all checks for a given endpoint land in the same partition, which is required for stateful window-based rule evaluation (error rate, latency percentiles).

---

## Database Schema

All migrations managed by Flyway. Migration files live in each service under `src/main/resources/db/migration/`.

### Core tables

```sql
-- V1__create_services.sql
CREATE TABLE services (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name        VARCHAR(100) NOT NULL UNIQUE,
    api_key     VARCHAR(255) NOT NULL,   -- bcrypt hashed
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    active      BOOLEAN NOT NULL DEFAULT true
);

-- V2__create_endpoints.sql
CREATE TABLE endpoints (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    service_id      UUID NOT NULL REFERENCES services(id),
    url             TEXT NOT NULL,
    http_method     VARCHAR(10) NOT NULL DEFAULT 'GET',
    headers         JSONB,
    timeout_ms      INT NOT NULL DEFAULT 5000,
    interval_secs   INT NOT NULL DEFAULT 60,
    active          BOOLEAN NOT NULL DEFAULT true,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- V3__create_sla_rules.sql
CREATE TABLE sla_rules (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    endpoint_id     UUID NOT NULL REFERENCES endpoints(id),
    rule_type       VARCHAR(30) NOT NULL,   -- AVAILABILITY, LATENCY, ERROR_RATE
    threshold_value NUMERIC NOT NULL,
    threshold_unit  VARCHAR(20) NOT NULL,
    window_seconds  INT NOT NULL,
    severity        VARCHAR(20) NOT NULL,   -- CRITICAL, WARNING, INFO
    sla_target      NUMERIC,               -- contractual SLA % (e.g. 99.9)
    enabled         BOOLEAN NOT NULL DEFAULT true
);

-- V4__create_checks.sql
CREATE TABLE checks (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    endpoint_id     UUID NOT NULL REFERENCES endpoints(id),
    checked_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    status_code     INT,
    latency_ms      INT,
    success         BOOLEAN NOT NULL,
    error_detail    TEXT
);
CREATE INDEX idx_checks_endpoint_time ON checks(endpoint_id, checked_at DESC);

-- V5__create_alerts.sql
CREATE TABLE alerts (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    endpoint_id     UUID NOT NULL REFERENCES endpoints(id),
    sla_rule_id     UUID NOT NULL REFERENCES sla_rules(id),
    status          VARCHAR(20) NOT NULL DEFAULT 'OPEN',
    severity        VARCHAR(20) NOT NULL,
    triggered_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
    acknowledged_at TIMESTAMPTZ,
    resolved_at     TIMESTAMPTZ,
    metadata        JSONB
);
CREATE INDEX idx_alerts_endpoint_status ON alerts(endpoint_id, status);
CREATE INDEX idx_alerts_triggered ON alerts(triggered_at DESC);
```

---

## Redis Usage

| Use Case | Data Structure | Key Pattern | TTL |
|---|---|---|---|
| Poll deduplication lock | `SET NX EX` | `poll-lock:{endpointId}` | interval_secs |
| Endpoint config cache | `STRING` (JSON) | `endpoint:{endpointId}` | 5 min |
| SLA rules cache | `STRING` (JSON) | `rules:{endpointId}` | 5 min |
| Cache invalidation | Pub/Sub | channel: `cache:invalidate` | — |
| Alert throttling | `SET NX EX` | `alert-throttle:{ruleId}` | configurable |
| API key lookup | `HASH` | `apikey:{hash}` | no TTL |
| Latency window buffer | `ZSET` | `latency:{endpointId}` | window_seconds |

---

## Module Structure (per Spring Boot service)

Each service follows a layered architecture with strict separation:

```
{service}/
└── src/main/java/com/slamonitor/{service}/
    ├── domain/
    │   ├── model/          # Pure domain entities (no framework deps)
    │   ├── service/        # Domain logic (SlaEvaluator, AlertThrottler)
    │   └── port/           # Interfaces (outbound ports)
    ├── application/
    │   └── usecase/        # Orchestration (EvaluateCheckUseCase, DispatchAlertUseCase)
    ├── infrastructure/
    │   ├── kafka/          # Consumers, Producers, DLT handlers
    │   ├── persistence/    # JPA Repositories, entity mappers
    │   ├── cache/          # Redis adapters
    │   ├── http/           # WebClient config, polling execution
    │   └── notification/   # Webhook, email, Slack dispatchers
    └── config/             # Spring beans, Kafka config, Redis config
```

---

## Docker Compose

All services run via Docker Compose for local development.

```yaml
# infra/docker-compose.yml
services:
  zookeeper:
    image: confluentinc/cp-zookeeper:7.6.0
    environment:
      ZOOKEEPER_CLIENT_PORT: 2181

  kafka:
    image: confluentinc/cp-kafka:7.6.0
    depends_on: [zookeeper]
    environment:
      KAFKA_BROKER_ID: 1
      KAFKA_ZOOKEEPER_CONNECT: zookeeper:2181
      KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://kafka:9092
      KAFKA_AUTO_CREATE_TOPICS_ENABLE: "false"
    ports:
      - "9092:9092"

  kafka-ui:
    image: provectuslabs/kafka-ui:latest
    depends_on: [kafka]
    ports:
      - "8090:8080"
    environment:
      KAFKA_CLUSTERS_0_BOOTSTRAPSERVERS: kafka:9092

  postgres:
    image: postgres:16
    environment:
      POSTGRES_DB: slamonitor
      POSTGRES_USER: sla
      POSTGRES_PASSWORD: sla
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data

  redis:
    image: redis:7-alpine
    command: redis-server --save 60 1
    ports:
      - "6379:6379"

  ingestor-service:
    build: ../services/ingestor-service
    depends_on: [kafka, postgres, redis]
    environment:
      SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/slamonitor
      SPRING_KAFKA_BOOTSTRAP_SERVERS: kafka:9092
      SPRING_REDIS_HOST: redis
    ports:
      - "8081:8080"

  sla-processor:
    build: ../services/sla-processor
    depends_on: [kafka, postgres, redis]
    environment:
      SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/slamonitor
      SPRING_KAFKA_BOOTSTRAP_SERVERS: kafka:9092
      SPRING_REDIS_HOST: redis
    ports:
      - "8082:8080"

  alert-service:
    build: ../services/alert-service
    depends_on: [kafka, postgres, redis]
    environment:
      SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/slamonitor
      SPRING_KAFKA_BOOTSTRAP_SERVERS: kafka:9092
      SPRING_REDIS_HOST: redis
    ports:
      - "8083:8080"

  frontend:
    build: ../frontend
    depends_on: [alert-service]
    ports:
      - "3000:3000"

volumes:
  postgres_data:
```

---

## Environment Variables

Each service reads configuration from environment variables. Secrets are never hardcoded.

### Common (all services)
```
SPRING_DATASOURCE_URL
SPRING_DATASOURCE_USERNAME
SPRING_DATASOURCE_PASSWORD
SPRING_KAFKA_BOOTSTRAP_SERVERS
SPRING_REDIS_HOST
SPRING_REDIS_PORT
```

### ingestor-service
```
POLLING_DEFAULT_INTERVAL_SECS=60
POLLING_DEFAULT_TIMEOUT_MS=5000
POLLING_MAX_CONCURRENT=50
```

### alert-service
```
ALERT_THROTTLE_DEFAULT_WINDOW_SECS=300
NOTIFICATION_SLACK_WEBHOOK_URL=
NOTIFICATION_SMTP_HOST=
NOTIFICATION_SMTP_PORT=
NOTIFICATION_SMTP_USER=
NOTIFICATION_SMTP_PASS=
```

---

## Development Workflow

### Start infrastructure
```bash
cd infra
docker compose up -d zookeeper kafka kafka-ui postgres redis
```

### Create Kafka topics
```bash
docker exec -it infra-kafka-1 kafka-topics --bootstrap-server localhost:9092 \
  --create --topic raw-checks --partitions 6 --replication-factor 1

docker exec -it infra-kafka-1 kafka-topics --bootstrap-server localhost:9092 \
  --create --topic sla-ok --partitions 3 --replication-factor 1

docker exec -it infra-kafka-1 kafka-topics --bootstrap-server localhost:9092 \
  --create --topic sla-violations --partitions 3 --replication-factor 1

docker exec -it infra-kafka-1 kafka-topics --bootstrap-server localhost:9092 \
  --create --topic raw-checks.DLT --partitions 1 --replication-factor 1
```

### Run a service locally (outside Docker)
```bash
cd services/ingestor-service
./mvnw spring-boot:run
```

### Run all services via Compose
```bash
cd infra
docker compose up --build
```

---

## Coding Conventions

- **Package naming**: `com.slamonitor.{service}` (e.g. `com.slamonitor.ingestor`)
- **Entity IDs**: always `UUID`, generated by the database (`gen_random_uuid()`)
- **Timestamps**: always `TIMESTAMPTZ` in PostgreSQL, `Instant` in Java
- **Error handling**: `@KafkaListener` methods use `DefaultErrorHandler` with exponential backoff; unrecoverable errors route to DLT
- **No `@Transactional` on Kafka consumers**: commit offset only after successful processing; handle failures explicitly
- **Redis keys**: always namespaced (`{context}:{identifier}`), never use raw IDs as keys
- **Flyway**: one concern per migration file, never alter existing migrations
- **DTOs**: never expose JPA entities directly from REST controllers; use dedicated request/response records
- **Secrets**: never commit `.env` files; use `.env.example` with placeholder values

---

## Key Design Decisions

**Why polling over agent/SDK?**
Zero friction for adoption — the client registers a URL and nothing else changes on their side. Simpler to sell and support for the target market (SMBs).

**Why Kafka instead of direct DB writes from the poller?**
Decouples check collection from rule evaluation. Allows independent scaling of polling throughput vs. processing complexity. Enables replay for rule backfilling when a new SLA rule is added to historical data.

**Why Redis for latency window buffers (ZSET)?**
Maintaining a rolling time window for p95 latency calculation in-memory per processor instance would break with multiple replicas. Redis ZSET with score = timestamp gives a shared, evictable window across all processor instances.

**Why SSE over WebSocket for the frontend?**
Alert delivery is unidirectional (server → client). SSE is simpler, HTTP/1.1 compatible, natively reconnects, and integrates cleanly with Next.js App Router streaming primitives. WebSocket would add unnecessary bidirectional complexity.

**Why Flyway over Liquibase?**
SQL-first migrations are more readable, easier to review in PRs, and avoid XML/YAML abstraction overhead. Flyway's sequential versioning enforces a clear migration history.

---

## Roadmap

### v1 — Core (MVP)
- [ ] Docker Compose with full infrastructure
- [ ] Endpoint registration API
- [ ] HTTP polling scheduler (ingestor-service)
- [ ] Basic AVAILABILITY rule evaluation (sla-processor)
- [ ] Alert persistence and dispatch via webhook (alert-service)
- [ ] Next.js dashboard: endpoint list + status

### v2 — SLA Depth
- [ ] LATENCY and ERROR_RATE rule types
- [ ] Rolling window metrics with Redis ZSET
- [ ] Alert state machine (OPEN → ACKNOWLEDGED → RESOLVED)
- [ ] SSE live alert feed on frontend

### v3 — Reporting
- [ ] Monthly SLA compliance report (contractual vs measured)
- [ ] Incident timeline (degradation start → resolution)
- [ ] CSV/PDF export

### v4 — Operational
- [ ] DLT consumer + dead letter inspection UI
- [ ] Per-endpoint polling health (is the poller running?)
- [ ] Alert throttling configuration per rule
- [ ] Multi-channel notifications (Slack, Email)

---

## Agent Rules

### After Every Completed Task

1. **Commit semantically** — no description body, no co-author line:
   ```
   git commit -m "type(scope): short imperative message"
   ```
   Types: `feat`, `fix`, `chore`, `refactor`, `test`, `docs`, `infra`

2. **Update `memory/`** — reflect the new state:
   - Tick off completed items in `memory/roadmap.md`
   - Add new entries to `memory/known-issues.md` if something was discovered or resolved
   - Update `memory/workflow.md` if the dev process changed

### Memory Directory

All agent memory lives in `memory/`. Always read it at the start of a new implementation task:

| File | Contents |
|---|---|
| `memory/MEMORY.md` | Index of all memory files |
| `memory/architecture.md` | Full system architecture |
| `memory/roadmap.md` | Feature roadmap + current status |
| `memory/known-issues.md` | Bugs, risks, watch points |
| `memory/decisions.md` | Architectural decisions with rationale |
| `memory/conventions.md` | Coding conventions + commit format |
| `memory/workflow.md` | Local dev workflow |