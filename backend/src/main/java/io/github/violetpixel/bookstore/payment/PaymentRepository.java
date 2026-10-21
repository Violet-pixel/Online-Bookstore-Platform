package io.github.violetpixel.bookstore.payment;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    List<Payment> findByOrderIdOrderByCreatedAtAscIdAsc(Long orderId);

    @EntityGraph(attributePaths = "order")
    Optional<Payment> findByGatewayReference(String reference);

    Optional<Payment> findByIdempotencyKey(String key);

    List<Payment> findByOrderIdAndStatus(Long orderId, PaymentStatus status);
}
