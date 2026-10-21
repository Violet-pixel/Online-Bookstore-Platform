package io.github.violetpixel.bookstore.common;

import java.time.Duration;
import java.util.UUID;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

/** Trava simples no Redis (SET NX PX) para tarefas que só uma instância deve rodar por vez. */
@Component
public class RedisLock {

    private final StringRedisTemplate redis;

    public RedisLock(StringRedisTemplate redis) {
        this.redis = redis;
    }

    public boolean runExclusive(String name, Duration ttl, Runnable task) {
        String key = "livraria:lock:" + name;
        String token = UUID.randomUUID().toString();
        Boolean acquired = redis.opsForValue().setIfAbsent(key, token, ttl);
        if (!Boolean.TRUE.equals(acquired)) {
            return false;
        }
        try {
            task.run();
            return true;
        } finally {
            if (token.equals(redis.opsForValue().get(key))) {
                redis.delete(key);
            }
        }
    }
}
