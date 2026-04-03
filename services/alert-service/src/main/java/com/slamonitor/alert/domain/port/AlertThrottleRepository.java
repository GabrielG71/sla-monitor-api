package com.slamonitor.alert.domain.port;

import java.util.UUID;

public interface AlertThrottleRepository {
    /**
     * Tries to acquire the throttle lock for the given rule.
     * Uses Redis SET NX EX — succeeds only if no recent alert was dispatched for this rule.
     *
     * @return true if dispatch should proceed, false if suppressed
     */
    boolean tryAcquire(UUID ruleId, int windowSeconds);
}
