package io.github.violetpixel.bookstore.auth;

import java.time.Duration;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import io.github.violetpixel.bookstore.common.ApiException;
import io.github.violetpixel.bookstore.common.Text;

/** Limita tentativas de login erradas por e-mail: 5 em 15 minutos. */
@Component
public class LoginThrottle {

    static final int MAX_FAILURES = 5;
    static final Duration WINDOW = Duration.ofMinutes(15);
    private static final String PREFIX = "livraria:auth:fail:";

    private final StringRedisTemplate redis;

    public LoginThrottle(StringRedisTemplate redis) {
        this.redis = redis;
    }

    public void check(String email) {
        String value = redis.opsForValue().get(key(email));
        if (value != null && Integer.parseInt(value) >= MAX_FAILURES) {
            throw new ApiException(HttpStatus.TOO_MANY_REQUESTS, "muitas_tentativas",
                    "Muitas tentativas. Espere 15 minutos e tente de novo.");
        }
    }

    public void failed(String email) {
        String key = key(email);
        Long count = redis.opsForValue().increment(key);
        if (count != null && count == 1) {
            redis.expire(key, WINDOW);
        }
    }

    public void succeeded(String email) {
        redis.delete(key(email));
    }

    private static String key(String email) {
        return PREFIX + Text.fold(email).replace(' ', '.');
    }
}
