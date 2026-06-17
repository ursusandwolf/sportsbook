package com.example.sportsbook.fraud;

import com.example.sportsbook.fraud.service.RedisVelocityService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RedisVelocityIntegrationTests {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private RedisVelocityService redisVelocityService;

    @Test
    void shouldIncrementVelocityCounter() {
        String key = "test:key";
        Duration duration = Duration.ofSeconds(10);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.increment(key)).thenReturn(1L, 2L);

        long count1 = redisVelocityService.incrementAndGet(key, duration);
        long count2 = redisVelocityService.incrementAndGet(key, duration);

        assertThat(count1).isEqualTo(1);
        assertThat(count2).isEqualTo(2);
        verify(redisTemplate, times(1)).expire(key, duration);
    }
}
