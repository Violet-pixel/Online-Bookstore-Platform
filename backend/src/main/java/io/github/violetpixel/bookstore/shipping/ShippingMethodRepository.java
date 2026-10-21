package io.github.violetpixel.bookstore.shipping;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ShippingMethodRepository extends JpaRepository<ShippingMethod, Long> {

    List<ShippingMethod> findAllByOrderBySortOrderAsc();

    Optional<ShippingMethod> findByCode(String code);
}
