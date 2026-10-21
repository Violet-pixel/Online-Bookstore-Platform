package io.github.violetpixel.bookstore.catalog;

import java.util.Collection;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import io.github.violetpixel.bookstore.config.CacheConfig;

/** Limpa o cache do catálogo depois do commit, quando estoque, preço ou vitrine mudam. */
@Component
public class CatalogCache {

    private final CacheManager caches;

    public CatalogCache(CacheManager caches) {
        this.caches = caches;
    }

    public void evictBooks(Collection<String> slugs) {
        afterCommit(() -> {
            Cache book = caches.getCache(CacheConfig.BOOK);
            if (book != null) {
                slugs.forEach(book::evict);
            }
            clear(CacheConfig.HOME);
            clear(CacheConfig.CATALOGS);
        });
    }

    public void evictAll() {
        afterCommit(() -> {
            clear(CacheConfig.BOOK);
            clear(CacheConfig.HOME);
            clear(CacheConfig.CATALOGS);
            clear(CacheConfig.CATEGORIES);
        });
    }

    private void clear(String name) {
        Cache cache = caches.getCache(name);
        if (cache != null) {
            cache.clear();
        }
    }

    private static void afterCommit(Runnable action) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    action.run();
                }
            });
        } else {
            action.run();
        }
    }
}
