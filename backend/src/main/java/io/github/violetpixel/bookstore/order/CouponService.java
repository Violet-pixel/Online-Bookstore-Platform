package io.github.violetpixel.bookstore.order;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.time.Instant;
import java.util.Locale;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.github.violetpixel.bookstore.common.Money;

@Service
public class CouponService {

    public record CouponResult(String code, boolean valid, BigDecimal discount, String message) {
        static CouponResult none() {
            return new CouponResult(null, false, Money.ZERO, null);
        }
    }

    private final CouponRepository coupons;

    public CouponService(CouponRepository coupons) {
        this.coupons = coupons;
    }

    @Transactional(readOnly = true)
    public CouponResult evaluate(String code, BigDecimal subtotal) {
        if (code == null || code.isBlank()) {
            return CouponResult.none();
        }
        String normalized = code.trim().toUpperCase(Locale.ROOT);
        Coupon c = coupons.findByCodeIgnoreCase(normalized).orElse(null);
        if (c == null || !c.isActive() || (c.getValidUntil() != null && c.getValidUntil().isBefore(Instant.now()))) {
            return new CouponResult(normalized, false, Money.ZERO, "Cupom inválido ou expirado.");
        }
        if (subtotal.compareTo(c.getMinSubtotal()) < 0) {
            String min = NumberFormat.getCurrencyInstance(Locale.of("pt", "BR")).format(c.getMinSubtotal());
            return new CouponResult(c.getCode(), false, Money.ZERO, "Este cupom vale para compras a partir de " + min + ".");
        }
        BigDecimal discount = c.getDiscountType() == Coupon.Type.PERCENT
                ? subtotal.multiply(c.getDiscountValue()).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_EVEN)
                : c.getDiscountValue();
        discount = Money.scale(discount.min(subtotal));
        return new CouponResult(c.getCode(), true, discount, c.getDescription());
    }
}
