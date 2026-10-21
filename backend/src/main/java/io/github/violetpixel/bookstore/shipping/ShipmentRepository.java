package io.github.violetpixel.bookstore.shipping;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ShipmentRepository extends JpaRepository<Shipment, Long> {

    Optional<Shipment> findByOrderId(Long orderId);

    long countByStatus(ShipmentStatus status);
}
