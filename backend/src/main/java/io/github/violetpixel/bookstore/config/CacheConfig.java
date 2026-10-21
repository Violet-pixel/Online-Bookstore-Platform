package io.github.violetpixel.bookstore.config;

import java.time.Duration;
import java.util.Map;

import org.springframework.boot.cache.autoconfigure.RedisCacheManagerBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;

/** Tempo de vida de cada cache do catálogo no Redis. */
@Configuration
public class CacheConfig {

    public static final String HOME = "home";
    public static final String CATEGORIES = "categories";
    public static final String CATALOGS = "catalogs";
    public static final String BOOK = "book";

    @Bean
    RedisCacheManagerBuilderCustomizer cacheTtls() {
        return builder -> Map.of(
                HOME, Duration.ofMinutes(5),
                CATEGORIES, Duration.ofMinutes(30),
                CATALOGS, Duration.ofMinutes(10),
                BOOK, Duration.ofMinutes(10))
            .forEach((name, ttl) -> builder.withCacheConfiguration(name,
                    RedisCacheConfiguration.defaultCacheConfig()
                        .entryTtl(ttl)
                        .prefixCacheNameWith("livraria:cache:")
                        .disableCachingNullValues()));
    }
}
