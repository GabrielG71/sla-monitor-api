package com.slamonitor.ingestor.infrastructure.cache;

import com.slamonitor.ingestor.domain.port.PollLockRepository;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.UUID;

@Repository
@SuppressWarnings("null")
public class PollLockRedisAdapter implements PollLockRepository {

    private final StringRedisTemplate redis;

    public PollLockRedisAdapter(StringRedisTemplate redis) {
        this.redis = redis;
    }

    @Override
    public boolean tryAcquire(UUID endpointId, int ttlSeconds) {
        String key = "poll-lock:" + endpointId;
        Boolean acquired = redis.opsForValue().setIfAbsent(key, "1", Duration.ofSeconds(ttlSeconds));
        return Boolean.TRUE.equals(acquired);
    }
}
