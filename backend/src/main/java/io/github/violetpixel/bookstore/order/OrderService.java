package io.github.violetpixel.bookstore.order;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import io.github.violetpixel.bookstore.auth.AppUser;
import io.github.violetpixel.bookstore.auth.UserRepository;
import io.github.violetpixel.bookstore.common.ApiException;
import io.github.violetpixel.bookstore.common.PageDto;
import io.github.violetpixel.bookstore.common.Text;
import io.github.violetpixel.bookstore.order.OrderDtos.AdvanceRequest;
import io.github.violetpixel.bookstore.order.OrderDtos.OrderDetailDto;
import io.github.violetpixel.bookstore.order.OrderDtos.OrderSummaryDto;
import io.github.violetpixel.bookstore.payment.Payment;
import io.github.violetpixel.bookstore.payment.PaymentRepository;
import io.github.violetpixel.bookstore.payment.PaymentStatus;
import jakarta.persistence.criteria.Predicate;

@Service
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    private final OrderRepository orders;
    private final OrderMapper mapper;
    private final FulfillmentService fulfillment;
    private final PaymentRepository payments;
    private final UserRepository users;
    private final TransactionTemplate tx;

    public OrderService(OrderRepository orders, OrderMapper mapper, FulfillmentService fulfillment, PaymentRepository payments,
            UserRepository users, PlatformTransactionManager txManager) {
        this.orders = orders;
        this.mapper = mapper;
        this.fulfillment = fulfillment;
        this.payments = payments;
        this.users = users;
        this.tx = new TransactionTemplate(txManager);
    }

    @Transactional(readOnly = true)
    public PageDto<OrderSummaryDto> mine(long userId, String status, int page, int size) {
        PageRequest pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 50),
                Sort.by(Sort.Order.desc("placedAt"), Sort.Order.desc("id")));
        OrderStatus filter = parse(status);
        Page<PurchaseOrder> result = filter == null ? orders.findByUserId(userId, pageable)
                : orders.findByUserIdAndStatus(userId, filter, pageable);
        return PageDto.of(result, o -> mapper.summary(o, false));
    }

    @Transactional(readOnly = true)
    public OrderDetailDto detail(long userId, String code) {
        return mapper.detail(own(userId, code), false);
    }

    @Transactional
    public OrderDetailDto cancel(long userId, String code, String reason) {
        PurchaseOrder order = own(userId, code);
        fulfillment.cancel(order, reason == null || reason.isBlank() ? "Cancelado por você." : reason.trim(), Instant.now());
        return mapper.detail(order, false);
    }

    PurchaseOrder own(long userId, String code) {
        return orders.findByCodeAndUserId(code, userId).orElseThrow(() -> ApiException.notFound("Pedido"));
    }

    /** Cancela pedidos cujo prazo de pagamento passou. Cada pedido roda na própria transação. */
    public int expireOverdue(Instant now) {
        List<Long> ids = orders.overdue(OrderStatus.PENDING_PAYMENT, now);
        int expired = 0;
        for (Long id : ids) {
            try {
                Boolean done = tx.execute(status -> {
                    PurchaseOrder order = orders.findById(id).orElse(null);
                    if (order == null || order.getStatus() != OrderStatus.PENDING_PAYMENT || order.getPaymentDueAt().isAfter(now)) {
                        return false;
                    }
                    for (Payment p : payments.findByOrderIdAndStatus(order.getId(), PaymentStatus.PENDING)) {
                        p.setStatus(PaymentStatus.EXPIRED);
                    }
                    fulfillment.cancel(order, "Pagamento não confirmado dentro do prazo.", now);
                    return true;
                });
                if (Boolean.TRUE.equals(done)) {
                    expired++;
                }
            } catch (RuntimeException e) {
                log.warn("Não foi possível expirar o pedido {}: {}", id, e.getMessage());
            }
        }
        if (expired > 0) {
            log.info("{} pedido(s) cancelado(s) por falta de pagamento.", expired);
        }
        return expired;
    }

    // ---------- administração ----------

    @Transactional(readOnly = true)
    public PageDto<OrderSummaryDto> adminList(String status, String q, int page, int size) {
        OrderStatus filter = parse(status);
        String term = q == null ? "" : q.trim();
        List<Long> userIds = new ArrayList<>();
        if (!term.isBlank() && term.contains("@")) {
            users.findByEmailIgnoreCase(term).map(AppUser::getId).ifPresent(userIds::add);
        }
        Specification<PurchaseOrder> spec = (root, query, cb) -> {
            List<Predicate> where = new ArrayList<>();
            if (filter != null) {
                where.add(cb.equal(root.get("status"), filter));
            }
            if (!term.isBlank()) {
                if (term.contains("@")) {
                    where.add(userIds.isEmpty() ? cb.disjunction() : root.get("userId").in(userIds));
                } else {
                    String like = "%" + term.toUpperCase(Locale.ROOT) + "%";
                    String folded = "%" + Text.fold(term) + "%";
                    where.add(cb.or(cb.like(root.get("code"), like),
                            cb.like(cb.lower(root.get("shipTo").get("recipient")), folded)));
                }
            }
            return cb.and(where.toArray(Predicate[]::new));
        };
        PageRequest pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100),
                Sort.by(Sort.Order.desc("placedAt"), Sort.Order.desc("id")));
        return PageDto.of(orders.findAll(spec, pageable), o -> mapper.summary(o, true));
    }

    @Transactional(readOnly = true)
    public OrderDetailDto adminDetail(String code) {
        return mapper.detail(byCode(code), true);
    }

    @Transactional
    public OrderDetailDto adminAdvance(String code, AdvanceRequest req) {
        PurchaseOrder order = byCode(code);
        fulfillment.advance(order, req, Instant.now());
        return mapper.detail(order, true);
    }

    @Transactional
    public OrderDetailDto adminCancel(String code, String reason) {
        PurchaseOrder order = byCode(code);
        fulfillment.cancel(order, reason == null || reason.isBlank() ? "Cancelado pela livraria." : reason.trim(), Instant.now());
        return mapper.detail(order, true);
    }

    private PurchaseOrder byCode(String code) {
        return orders.findByCode(code).orElseThrow(() -> ApiException.notFound("Pedido"));
    }

    private static OrderStatus parse(String status) {
        if (status == null || status.isBlank() || "ALL".equalsIgnoreCase(status)) {
            return null;
        }
        try {
            return OrderStatus.valueOf(status.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw ApiException.badRequest("status_invalido", "Status de pedido desconhecido.");
        }
    }
}
