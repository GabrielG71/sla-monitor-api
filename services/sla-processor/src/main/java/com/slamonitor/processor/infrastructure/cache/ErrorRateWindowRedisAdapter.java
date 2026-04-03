package com.slamonitor.processor.infrastructure.cache;

import com.slamonitor.processor.domain.port.ErrorRateWindowRepository;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

/**
 * Redis ZSET adapter for rolling error-rate windows.
 *
 * Key:    errors:{endpointId}
 * Score:  epoch millis (used for time-based eviction)
 * Member: "{epochMillis}:{outcome}" where outcome = "1" (failure) or "0" (success)
 */
@Repository
@SuppressWarnings("null")
public class ErrorRateWindowRedisAdapter implements ErrorRateWindowRepository {

    private final StringRedisTemplate redis;

    public ErrorRateWindowRedisAdapter(StringRedisTemplate redis) {
        this.redis = redis;
    }

    @Override
    public void record(UUID endpointId, boolean success, int windowSeconds) {
        String key = "errors:" + endpointId;
        long now = Instant.now().toEpochMilli();
        long cutoff = now - (long) windowSeconds * 1_000;

        redis.opsForZSet().add(key, now + ":" + (success ? "0" : "1"), now);
        redis.opsForZSet().removeRangeByScore(key, 0, cutoff);
    }

    @Override
    public double getErrorRate(UUID endpointId) {
        String key = "errors:" + endpointId;
        Set<String> members = redis.opsForZSet().range(key, 0, -1);
        if (members == null || members.isEmpty()) return 0;

        long failures = members.stream().filter(m -> m.endsWith(":1")).count();
        return (double) failures / members.size() * 100.0;
    }
}
