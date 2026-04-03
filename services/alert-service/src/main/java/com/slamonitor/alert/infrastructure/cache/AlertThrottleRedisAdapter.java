package com.slamonitor.alert.infrastructure.cache;

import com.slamonitor.alert.domain.port.AlertThrottleRepository;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.UUID;

@Repository
@SuppressWarnings("null")
public class AlertThrottleRedisAdapter implements AlertThrottleRepository {

    private final StringRedisTemplate redis;

    public AlertThrottleRedisAdapter(StringRedisTemplate redis) {
        this.redis = redis;
    }

    @Override
    public boolean tryAcquire(UUID ruleId, int windowSeconds) {
        String key = "alert-throttle:" + ruleId;
        Boolean acquired = redis.opsForValue().setIfAbsent(key, "1", Duration.ofSeconds(windowSeconds));
        return Boolean.TRUE.equals(acquired);
    }
}
