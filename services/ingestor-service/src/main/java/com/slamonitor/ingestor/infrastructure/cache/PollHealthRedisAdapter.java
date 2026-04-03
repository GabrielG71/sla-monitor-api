package com.slamonitor.ingestor.infrastructure.cache;

import com.slamonitor.ingestor.domain.model.CheckResult;
import com.slamonitor.ingestor.domain.model.PollHealth;
import com.slamonitor.ingestor.domain.port.PollHealthRepository;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/**
 * Records the most recent poll outcome per endpoint in Redis.
 *
 * Key:   poll-last:{endpointId}
 * Value: "{checkedAt}|{success}|{statusCode}|{latencyMs}"
 * TTL:   interval_secs × 3  (missing 3 consecutive polls → key expires → unknown health)
 */
@Repository
@SuppressWarnings("null")
public class PollHealthRedisAdapter implements PollHealthRepository {

    private final StringRedisTemplate redis;

    public PollHealthRedisAdapter(StringRedisTemplate redis) {
        this.redis = redis;
    }

    @Override
    public void record(CheckResult result, int ttlSeconds) {
        String key = "poll-last:" + result.endpointId();
        String value = result.checkedAt() + "|"
                + result.success() + "|"
                + (result.statusCode() != null ? result.statusCode() : "") + "|"
                + (result.latencyMs() != null ? result.latencyMs() : "");
        redis.opsForValue().set(key, value, Duration.ofSeconds((long) ttlSeconds * 3));
    }

    @Override
    public Optional<PollHealth> getLatest(UUID endpointId) {
        String raw = redis.opsForValue().get("poll-last:" + endpointId);
        if (raw == null) return Optional.empty();
        String[] parts = raw.split("\\|", -1);
        if (parts.length < 4) return Optional.empty();
        return Optional.of(new PollHealth(
                endpointId,
                Instant.parse(parts[0]),
                Boolean.parseBoolean(parts[1]),
                parts[2].isEmpty() ? null : Integer.parseInt(parts[2]),
                parts[3].isEmpty() ? null : Integer.parseInt(parts[3])
        ));
    }
}
