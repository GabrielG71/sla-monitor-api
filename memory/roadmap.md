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

**Phase**: v1 complete — all three backend services + Next.js dashboard implemented.
**Next step**: v2 — LATENCY_P95/ERROR_RATE rules, Redis ZSET rolling windows, alert state machine, SSE live feed.
