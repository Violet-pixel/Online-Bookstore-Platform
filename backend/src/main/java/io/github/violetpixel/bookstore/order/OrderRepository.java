package io.github.violetpixel.bookstore.order;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OrderRepository extends JpaRepository<PurchaseOrder, Long>, JpaSpecificationExecutor<PurchaseOrder> {

    Optional<PurchaseOrder> findByCode(String code);

    Optional<PurchaseOrder> findByCodeAndUserId(String code, Long userId);

    boolean existsByCode(String code);

    Page<PurchaseOrder> findByUserId(Long userId, Pageable pageable);

    Page<PurchaseOrder> findByUserIdAndStatus(Long userId, OrderStatus status, Pageable pageable);

    @Query("select o.id from PurchaseOrder o where o.status = :status and o.paymentDueAt < :now")
    List<Long> overdue(@Param("status") OrderStatus status, @Param("now") Instant now);

    long countByStatus(OrderStatus status);

    long countByPlacedAtAfter(Instant since);

    @Query("select coalesce(sum(o.total), 0) from PurchaseOrder o where o.paidAt is not null and o.paidAt >= :since "
            + "and o.status <> io.github.violetpixel.bookstore.order.OrderStatus.CANCELED")
    BigDecimal revenueSince(@Param("since") Instant since);

    @Query("select count(o) from PurchaseOrder o where o.paidAt is not null and o.paidAt >= :since "
            + "and o.status <> io.github.violetpixel.bookstore.order.OrderStatus.CANCELED")
    long paidCountSince(@Param("since") Instant since);

    @Query("select o.status, count(o) from PurchaseOrder o group by o.status")
    List<Object[]> countByStatusGrouped();
}
