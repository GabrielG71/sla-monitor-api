# Known Issues

## Open Issues

### 5. Lazy-loading `service` in `EndpointResponse`
- **Risk**: `EndpointResponse.from(endpoint)` calls `endpoint.getService().getId()`, which triggers a lazy load. Must be called within an open JPA session (inside a `@Transactional` context or within the request scope with `open-in-view: false`).
- **Mitigation**: All write paths go through use cases annotated with `@Transactional`. Read paths in the controller use `endpointRepository.findById()` which loads the full entity — the lazy load fires within the same JPA session opened by the Spring Data proxy. No action needed now, but watch if this causes `LazyInitializationException` during integration testing.
- **Status**: Observed during implementation, not yet tested.

---

## Resolved Issues

_None yet._

---

## Design Risks / Watch Points

### 1. Overlapping polls on slow endpoints
- **Risk**: If an endpoint response takes longer than its polling interval, the next poll fires before the previous one finishes.
- **Mitigation**: Redis `SET NX EX` lock (`poll-lock:{endpointId}`) with TTL = interval_secs skips the next poll if the previous is still running.
- **Status**: Implemented in `PollLockRedisAdapter` + `PollEndpointUseCase`.

### 2. Stateful window evaluation across sla-processor replicas
- **Risk**: `AvailabilityEvaluator` tracks consecutive failures in a `ConcurrentHashMap` — state is local to one instance. Safe because Kafka partitions by `endpointId` (all checks for an endpoint always hit the same consumer). Will break if replicas > partitions or on rebalance.
- **Mitigation**: Redis ZSET (`latency:{endpointId}`) for shared window state — planned for v2.
- **Status**: In-memory version implemented; Redis migration deferred to v2.

### 3. Kafka consumer group rebalancing during window evaluation
- **Risk**: Partition reassignment during a rebalance can cause in-progress window calculations to reset or double-count.
- **Mitigation**: Keying by `endpointId` ensures partition affinity. Needs careful handling in consumer offset commit strategy.
- **Status**: To be addressed during sla-processor implementation.

### 4. Flyway migration ownership across services
- **Risk**: Three services share the same PostgreSQL database. If two services run migrations simultaneously, conflicts may occur.
- **Mitigation**: Flyway uses a distributed lock table. Only `ingestor-service` should own migrations V1–V4; `alert-service` owns V5. To be enforced via `spring.flyway.locations` config per service.
- **Status**: To be designed before first migration run.
