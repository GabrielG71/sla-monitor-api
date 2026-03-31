# Design Decisions

## Why polling over agent/SDK?
Zero friction for the client — they register a URL, nothing changes on their side. Simpler to sell and support for the target market (SMBs).

## Why Kafka instead of direct DB writes from the poller?
- Decouples check collection from rule evaluation
- Allows independent scaling: polling throughput vs. processing complexity
- Enables **replay**: when a new SLA rule is added, historical `raw-checks` events can be replayed to backfill violations

## Why Redis ZSET for latency window buffers?
- Maintaining rolling time windows per processor instance in-memory would break under multiple replicas
- Redis ZSET with score = timestamp gives a shared, evictable window across all processor instances

## Why SSE over WebSocket for the frontend?
- Alert delivery is unidirectional (server → client)
- SSE: simpler, HTTP/1.1 compatible, natively reconnects, integrates cleanly with Next.js App Router streaming
- WebSocket adds unnecessary bidirectional complexity

## Why Flyway over Liquibase?
- SQL-first migrations are more readable and easier to review in PRs
- Avoids XML/YAML abstraction overhead
- Sequential versioning enforces clear migration history

## Why Confluent Platform 7.6 over vanilla Kafka?
- Better tooling compatibility (Kafka UI, Schema Registry if needed later)
- Single cohesive versioning for Kafka + Zookeeper images

## Why hexagonal (ports & adapters) per service?
- Domain logic isolated from infrastructure (Kafka, Redis, DB)
- Enables testing domain logic without spinning up containers
- Clean boundaries make it easier to swap infrastructure later (e.g., replace Redis with another cache)
