# Roadmap

## v1 — Core (MVP)
- [x] Docker Compose with full infrastructure (zookeeper, kafka, kafka-ui, postgres, redis)
- [x] `infra/kafka/init-topics.sh` — topic creation script
- [x] Maven multi-module structure (parent pom.xml + wrapper at root)
- [x] Flyway migrations V1–V5 (services, endpoints, sla_rules, checks, alerts)
- [x] Next.js frontend scaffolded (create-next-app — TypeScript, Tailwind, App Router)
- [ ] `ingestor-service`: endpoint registration API (POST/PUT/DELETE /endpoints)
- [ ] `ingestor-service`: HTTP polling scheduler with WebClient
- [ ] `sla-processor`: basic AVAILABILITY rule evaluation
- [ ] `alert-service`: alert persistence + webhook dispatch
- [ ] Next.js dashboard: endpoint list + current status

## v2 — SLA Depth
- [ ] `sla-processor`: LATENCY_P95 and ERROR_RATE rule types
- [ ] Rolling window metrics with Redis ZSET
- [ ] Alert state machine (OPEN → ACKNOWLEDGED → RESOLVED) in alert-service
- [ ] SSE live alert feed on frontend (`/alerts/stream`)

## v3 — Reporting
- [ ] Monthly SLA compliance report (contractual vs measured)
- [ ] Incident timeline (degradation start → resolution)
- [ ] CSV/PDF export

## v4 — Operational
- [ ] DLT consumer + dead letter inspection UI
- [ ] Per-endpoint polling health indicator
- [ ] Alert throttling configuration per rule (UI)
- [ ] Multi-channel notifications: Slack + Email (SMTP)

---

## Current Status

**Phase**: Scaffolding complete — structure and infra in place, no business logic yet.
**Next step**: Implement `ingestor-service` endpoint registration API (POST/PUT/DELETE /endpoints).
