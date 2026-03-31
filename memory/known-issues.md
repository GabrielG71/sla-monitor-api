# Known Issues

## Open Issues

_None yet — project is in scaffolding phase._

---

## Resolved Issues

_None yet._

---

## Design Risks / Watch Points

### 1. Overlapping polls on slow endpoints
- **Risk**: If an endpoint response takes longer than its polling interval, the next poll fires before the previous one finishes.
- **Mitigation**: Redis `SET NX EX` lock (`poll-lock:{endpointId}`) with TTL = interval_secs skips the next poll if the previous is still running.
- **Status**: Designed, not yet implemented.

### 2. Stateful window evaluation across sla-processor replicas
- **Risk**: Rolling window (p95 latency, error rate) calculated in-memory would be inconsistent across multiple processor instances.
- **Mitigation**: Redis ZSET (`latency:{endpointId}`) provides shared window state across all replicas.
- **Status**: Designed, not yet implemented.

### 3. Kafka consumer group rebalancing during window evaluation
- **Risk**: Partition reassignment during a rebalance can cause in-progress window calculations to reset or double-count.
- **Mitigation**: Keying by `endpointId` ensures partition affinity. Needs careful handling in consumer offset commit strategy.
- **Status**: To be addressed during sla-processor implementation.

### 4. Flyway migration ownership across services
- **Risk**: Three services share the same PostgreSQL database. If two services run migrations simultaneously, conflicts may occur.
- **Mitigation**: Flyway uses a distributed lock table. Only `ingestor-service` should own migrations V1–V4; `alert-service` owns V5. To be enforced via `spring.flyway.locations` config per service.
- **Status**: To be designed before first migration run.
