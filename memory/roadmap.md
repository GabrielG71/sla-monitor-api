# Roadmap

## v1 — Core (MVP)
- [x] Docker Compose with full infrastructure (zookeeper, kafka, kafka-ui, postgres, redis)
- [x] `infra/kafka/init-topics.sh` — topic creation script
- [x] Maven multi-module structure (parent pom.xml + wrapper at root)
- [x] Flyway migrations V1–V5 (services, endpoints, sla_rules, checks, alerts)
- [x] Next.js frontend scaffolded (create-next-app — TypeScript, Tailwind, App Router)
- [x] `ingestor-service`: endpoint registration API (POST/PUT/DELETE /endpoints + GET list/by-id)
- [x] `ingestor-service`: HTTP polling scheduler with WebClient
- [x] `sla-processor`: basic AVAILABILITY rule evaluation
- [x] `alert-service`: alert persistence + webhook dispatch
- [x] Next.js dashboard: endpoint list + current status

## v2 — SLA Depth
- [x] `sla-processor`: LATENCY_P95 and ERROR_RATE rule types
- [x] Rolling window metrics with Redis ZSET
- [x] Alert state machine (OPEN → ACKNOWLEDGED → RESOLVED) in alert-service
- [x] SSE live alert feed on frontend (`/alerts/stream`)

## v3 — Reporting
- [x] Monthly SLA compliance report (contractual vs measured)
- [x] Incident timeline (degradation start → resolution)
- [x] CSV export (PDF deferred — no library dependency justified yet)

## v4 — Operational
- [x] DLT consumer + dead letter inspection UI
- [x] Per-endpoint polling health indicator (`poll-last:{endpointId}` in Redis, GET /endpoints/{id}/health)
- [x] Alert throttle uses per-rule `windowSeconds` (from SlaViolation payload)
- [x] Multi-channel notifications: Slack + Email (SMTP)

---

## Current Status

**Phase**: v4 complete — all roadmap items delivered.
**Next step**: production hardening (integration tests, API auth, k8s manifests) or new feature requests.
