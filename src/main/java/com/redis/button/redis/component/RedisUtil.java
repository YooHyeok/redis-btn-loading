package com.redis.button.redis.component;

import io.lettuce.core.dynamic.domain.Timeout;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class RedisUtil {
    private final RedisTemplate redisTemplate;

    /* IN-MEMMORY */
    public boolean setString(String key, String value) {
        try {
            redisTemplate.opsForValue().set(key, value);
            return true;
        } catch (Exception e) {
            log.error("Redis set 실패", e);
            return false;
        }
    }

    public boolean setString(String key, String value, long second) {
        if (second == 0) throw new IllegalArgumentException("Zero value is not allowed.");
        try {
            redisTemplate.opsForValue().set(key, value, Duration.ofSeconds(second));
            return true;
        } catch (Exception e) {
            log.error("Redis set 실패", e);
            return false;
        }
    }

    public String getString(String key) {
        try {
            Object value = redisTemplate.opsForValue().get(key);
            return (value instanceof String) ? (String) value : null;
        } catch (Exception e) {
            log.error("Redis get 실패: {}", key, e);
            return null;
        }
    }

    public boolean isExistString(String key) {
        String value = getString(key);
        return value != null ? true : false;
    }


    public String getStringWithDelete(String key) {
        String value = getString(key);
        try {
            if (value != null) redisTemplate.delete(key);
            return value;
        } catch (Exception e) {
            log.error("Redis delete 실패: {}", key, e);
            return value;
        }
    }

}
