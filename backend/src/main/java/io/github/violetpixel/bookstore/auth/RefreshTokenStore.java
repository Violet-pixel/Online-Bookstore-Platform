package io.github.violetpixel.bookstore.auth;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Optional;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import io.github.violetpixel.bookstore.config.AppProperties;

/**
 * Tokens de renovação ficam no Redis (só o hash SHA-256), com expiração e rotação a cada uso.
 * Sair da conta apaga a chave e invalida o token na hora.
 */
@Component
public class RefreshTokenStore {

    private static final String PREFIX = "livraria:auth:refresh:";
    private final StringRedisTemplate redis;
    private final AppProperties props;
    private final SecureRandom random = new SecureRandom();

    public RefreshTokenStore(StringRedisTemplate redis, AppProperties props) {
        this.redis = redis;
        this.props = props;
    }

    public String issue(long userId) {
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        redis.opsForValue().set(PREFIX + hash(token), String.valueOf(userId), props.jwt().refreshTtl());
        return token;
    }

    /** Consome o token (uso único) e devolve o id do dono. */
    public Optional<Long> consume(String token) {
        if (token == null || token.isBlank()) {
            return Optional.empty();
        }
        String value = redis.opsForValue().getAndDelete(PREFIX + hash(token));
        return Optional.ofNullable(value).map(Long::valueOf);
    }

    public void revoke(String token) {
        if (token != null && !token.isBlank()) {
            redis.delete(PREFIX + hash(token));
        }
    }

    public long ttlSeconds() {
        return props.jwt().refreshTtl().toSeconds();
    }

    static String hash(String token) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
