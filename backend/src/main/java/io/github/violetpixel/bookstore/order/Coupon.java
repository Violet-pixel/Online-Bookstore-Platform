package io.github.violetpixel.bookstore.order;

import java.math.BigDecimal;
import java.time.Instant;

import io.github.violetpixel.bookstore.common.Flag;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "coupon")
public class Coupon {

    public enum Type { PERCENT, FIXED }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 30)
    private String code;

    @Column(nullable = false, length = 160)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Type discountType;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal discountValue;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal minSubtotal;

    private Instant validUntil;

    @Convert(converter = Flag.class)
    @Column(nullable = false)
    private Boolean active = Boolean.TRUE;

    protected Coupon() { }

    public Coupon(String code, String description, Type type, BigDecimal value, BigDecimal minSubtotal, Instant validUntil) {
        this.code = code;
        this.description = description;
        this.discountType = type;
        this.discountValue = value;
        this.minSubtotal = minSubtotal;
        this.validUntil = validUntil;
    }

    public Long getId() { return id; }
    public String getCode() { return code; }
    public String getDescription() { return description; }
    public Type getDiscountType() { return discountType; }
    public BigDecimal getDiscountValue() { return discountValue; }
    public BigDecimal getMinSubtotal() { return minSubtotal; }
    public Instant getValidUntil() { return validUntil; }
    public boolean isActive() { return Boolean.TRUE.equals(active); }
}
