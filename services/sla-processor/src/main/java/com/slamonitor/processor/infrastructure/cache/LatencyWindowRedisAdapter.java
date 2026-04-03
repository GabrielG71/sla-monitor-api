package com.slamonitor.processor.infrastructure.cache;

import com.slamonitor.processor.domain.port.LatencyWindowRepository;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Redis ZSET adapter for p95 latency rolling windows.
 *
 * Key:    latency:{endpointId}
 * Score:  epoch millis (used for time-based eviction)
 * Member: "{epochMillis}:{latencyMs}" (unique per entry within the same millisecond and latency)
 */
@Repository
@SuppressWarnings("null")
public class LatencyWindowRedisAdapter implements LatencyWindowRepository {

    private final StringRedisTemplate redis;

    public LatencyWindowRedisAdapter(StringRedisTemplate redis) {
        this.redis = redis;
    }

    @Override
    public void record(UUID endpointId, int latencyMs, int windowSeconds) {
        String key = "latency:" + endpointId;
        long now = Instant.now().toEpochMilli();
        long cutoff = now - (long) windowSeconds * 1_000;

        redis.opsForZSet().add(key, now + ":" + latencyMs, now);
        redis.opsForZSet().removeRangeByScore(key, 0, cutoff);
    }

    @Override
    public double getPercentile(UUID endpointId, double percentile) {
        String key = "latency:" + endpointId;
        Set<String> members = redis.opsForZSet().range(key, 0, -1);
        if (members == null || members.isEmpty()) return 0;

        List<Integer> sorted = members.stream()
                .map(m -> Integer.parseInt(m.split(":")[1]))
                .sorted()
                .toList();

        int index = (int) Math.ceil(percentile / 100.0 * sorted.size()) - 1;
        index = Math.max(0, Math.min(index, sorted.size() - 1));
        return sorted.get(index);
    }
}
