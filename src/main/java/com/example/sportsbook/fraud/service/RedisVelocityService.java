package com.example.sportsbook.fraud.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@Slf4j
@RequiredArgsConstructor
public class RedisVelocityService {

    private final StringRedisTemplate redisTemplate;

    public long incrementAndGet(String key, Duration duration) {
        try {
            Long count = redisTemplate.opsForValue().increment(key);
            if (count != null && count == 1) {
                redisTemplate.expire(key, duration);
            }
            return count != null ? count : 0;
        } catch (Exception e) {
            log.error("Redis error, skipping velocity count for key: {}", key, e);
            // Fallback: return 0 to allow the operation if Redis is down
            return 0; 
        }
    }
}
