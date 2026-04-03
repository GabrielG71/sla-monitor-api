package com.slamonitor.ingestor.domain.port;

import java.util.UUID;

public interface PollLockRepository {
    /**
     * Attempts to acquire the poll lock for the given endpoint.
     * Uses Redis SET NX EX — succeeds only if no lock exists.
     * TTL = intervalSecs, so the lock expires exactly when the next poll is due.
     *
     * @return true if the lock was acquired (poll should proceed), false if already locked (skip)
     */
    boolean tryAcquire(UUID endpointId, int ttlSeconds);
}
