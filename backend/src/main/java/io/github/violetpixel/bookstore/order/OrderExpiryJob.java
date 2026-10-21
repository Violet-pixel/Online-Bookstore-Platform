package io.github.violetpixel.bookstore.order;

import java.time.Duration;
import java.time.Instant;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import io.github.violetpixel.bookstore.common.RedisLock;

/** A cada minuto cancela pedidos não pagos no prazo. A trava no Redis evita duas instâncias rodando juntas. */
@Component
@ConditionalOnProperty(name = "app.jobs.enabled", havingValue = "true", matchIfMissing = true)
public class OrderExpiryJob {

    private final OrderService orders;
    private final RedisLock lock;

    public OrderExpiryJob(OrderService orders, RedisLock lock) {
        this.orders = orders;
        this.lock = lock;
    }

    @Scheduled(initialDelayString = "PT30S", fixedDelayString = "PT1M")
    public void run() {
        lock.runExclusive("order-expiry", Duration.ofSeconds(50), () -> orders.expireOverdue(Instant.now()));
    }
}
