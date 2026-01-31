package cn.cordys.common.service;

import jakarta.annotation.Resource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class IdempotentService {

    private static final String IDEMPOTENT_KEY_PREFIX = "idempotent:";
    private static final long DEFAULT_EXPIRE_TIME = 5;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    public boolean checkAndSet(String key) {
        return checkAndSet(key, DEFAULT_EXPIRE_TIME, TimeUnit.SECONDS);
    }

    public boolean checkAndSet(String key, long expireTime, TimeUnit timeUnit) {
        String redisKey = IDEMPOTENT_KEY_PREFIX + key;
        Boolean result = redisTemplate.opsForValue().setIfAbsent(redisKey, "1", expireTime, timeUnit);
        return Boolean.TRUE.equals(result);
    }

    public void remove(String key) {
        String redisKey = IDEMPOTENT_KEY_PREFIX + key;
        redisTemplate.delete(redisKey);
    }

    public boolean exists(String key) {
        String redisKey = IDEMPOTENT_KEY_PREFIX + key;
        return Boolean.TRUE.equals(redisTemplate.hasKey(redisKey));
    }
}
