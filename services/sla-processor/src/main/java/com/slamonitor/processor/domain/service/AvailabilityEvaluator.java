package com.slamonitor.processor.domain.service;

import com.slamonitor.processor.domain.model.SlaRule;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tracks consecutive failures per endpoint in memory.
 *
 * Safe for a single replica because Kafka partitions by endpointId — all checks
 * for a given endpoint always land on the same consumer instance.
 * Multi-replica support will migrate this state to Redis ZSET in v2.
 */
@Service
@SuppressWarnings("null")
public class AvailabilityEvaluator {

    private final ConcurrentHashMap<UUID, Integer> consecutiveFailures = new ConcurrentHashMap<>();

    /**
     * Evaluates whether the given check result constitutes an AVAILABILITY violation.
     *
     * @return the current consecutive failure count if it meets or exceeds the threshold
     *         (caller should publish a violation), or 0 if no violation.
     */
    public int evaluate(SlaRule rule, boolean checkSuccess) {
        UUID endpointId = rule.getEndpointId();

        if (checkSuccess) {
            consecutiveFailures.remove(endpointId);
            return 0;
        }

        int failures = consecutiveFailures.merge(endpointId, 1, Integer::sum);
        int threshold = rule.getThresholdValue().intValue();

        return failures >= threshold ? failures : 0;
    }
}
