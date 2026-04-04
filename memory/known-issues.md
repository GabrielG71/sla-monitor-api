# Known Issues

## Open Issues

### 5. Lazy-loading `service` in `EndpointResponse`
- **Risk**: `EndpointResponse.from(endpoint)` calls `endpoint.getService().getId()`, which triggers a lazy load. Must be called within an open JPA session (inside a `@Transactional` context or within the request scope with `open-in-view: false`).
- **Mitigation**: All write paths go through use cases annotated with `@Transactional`. Read paths in the controller use `endpointRepository.findById()` which loads the full entity — the lazy load fires within the same JPA session opened by the Spring Data proxy. No action needed now, but watch if this causes `LazyInitializationException` during integration testing.
- **Status**: Observed during implementation, not yet tested.

---

## Resolved Issues

### Maven parent POM missing repackage goal and -parameters flag
- **Symptom 1**: Docker containers crashed with `no main manifest attribute, in app.jar`. The parent POM declared `spring-boot-maven-plugin` in `pluginManagement` but without an `<execution>` for the `repackage` goal. Without `spring-boot-starter-parent` as parent, the goal is not inherited automatically — `mvn package` produced a plain JAR with no `Main-Class` in the manifest.
- **Symptom 2**: `@PathVariable UUID id` endpoints returned 500 with "Name for argument of type [UUID] not specified... Ensure that the compiler uses the '-parameters' flag." Spring MVC resolves parameter names via reflection; without `-parameters`, the JVM omits them.
- **Fix**: Added `<goal>repackage</goal>` execution and `maven-compiler-plugin` with `<arg>-parameters</arg>` to the root `pom.xml` `pluginManagement` block. Both fixes apply to all three services via inheritance.
- **Status**: Resolved. Committed in `2076619`.

### WebhookDispatcher port replaced by NotificationDispatcher
- **Context**: v4 refactored single-channel webhook dispatch to multi-channel `List<NotificationDispatcher>`.
- `WebhookDispatcher` port deleted; `WebhookWebClientDispatcher`, `SlackWebClientDispatcher`, `EmailJavaMailDispatcher` all implement `NotificationDispatcher`.
- `DispatchAlertUseCase` iterates all enabled dispatchers per alert.

### 6. docker-compose frontend env vars missing
- **Risk**: `frontend` service in `docker-compose.yml` sets `NEXT_PUBLIC_API_URL` which is not used by the Next.js app. The actual vars `INGESTOR_URL`, `ALERT_URL`, `PROCESSOR_URL` are not set, so the app falls back to `localhost` defaults — which are unreachable inside Docker.
- **Mitigation**: Set the three correct env vars in `infra/docker-compose.yml` pointing to internal service names (`http://ingestor-service:8080`, `http://alert-service:8080`, `http://sla-processor:8080`). Remove the unused `NEXT_PUBLIC_API_URL`.
- **Status**: Identified. Fix applied in docker-compose.yml.

---

## Design Risks / Watch Points

### 1. Overlapping polls on slow endpoints
- **Risk**: If an endpoint response takes longer than its polling interval, the next poll fires before the previous one finishes.
- **Mitigation**: Redis `SET NX EX` lock (`poll-lock:{endpointId}`) with TTL = interval_secs skips the next poll if the previous is still running.
- **Status**: Implemented in `PollLockRedisAdapter` + `PollEndpointUseCase`.

### 2. Stateful window evaluation across sla-processor replicas
- **Risk**: `AvailabilityEvaluator` tracks consecutive failures in a `ConcurrentHashMap` — state is local to one instance. Safe because Kafka partitions by `endpointId`.
- **LATENCY/ERROR_RATE**: fully shared via Redis ZSET (`latency:{endpointId}`, `errors:{endpointId}`).
- **AVAILABILITY**: still in-memory ConcurrentHashMap — safe with single replica, but will reset on rebalance.
- **Status**: LATENCY/ERROR_RATE resolved in v2. AVAILABILITY migration to Redis deferred (low priority — single-replica deployment assumed).

### 3. Kafka consumer group rebalancing during window evaluation
- **Risk**: Partition reassignment during a rebalance can cause in-progress window calculations to reset or double-count.
- **Mitigation**: Keying by `endpointId` ensures partition affinity. LATENCY/ERROR_RATE windows live in Redis (shared, survives rebalance). AVAILABILITY counter is in-memory — resets on rebalance but single-replica deployment is assumed.
- **Status**: Theoretical risk, not actively mitigated beyond partition affinity. Acceptable for current scale.

### 4. Flyway migration ownership across services
- **Risk**: Three services share the same PostgreSQL database. If two services run migrations simultaneously, conflicts may occur.
- **Resolution**: `ingestor-service` owns all migrations (V1–V6). `sla-processor` and `alert-service` have `spring.flyway.enabled: false`. Only one service ever runs Flyway.
- **Status**: Resolved in v1.
