package io.github.violetpixel.bookstore.shipping;

import java.math.BigDecimal;

import io.github.violetpixel.bookstore.common.Flag;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "shipping_method")
public class ShippingMethod {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String code;

    @Column(nullable = false, length = 60)
    private String name;

    @Column(nullable = false, length = 60)
    private String carrier;

    @Column(length = 200)
    private String description;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal baseFee;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal feePerKg;

    @Column(precision = 12, scale = 2)
    private BigDecimal freeAbove;

    @Column(nullable = false)
    private Integer minDays;

    @Column(nullable = false)
    private Integer maxDays;

    @Column(nullable = false)
    private Integer sortOrder = 0;

    @Convert(converter = Flag.class)
    @Column(nullable = false)
    private Boolean active = Boolean.TRUE;

    protected ShippingMethod() { }

    public ShippingMethod(String code, String name, String carrier, String description, BigDecimal baseFee,
            BigDecimal feePerKg, BigDecimal freeAbove, int minDays, int maxDays, int sortOrder) {
        this.code = code;
        this.name = name;
        this.carrier = carrier;
        this.description = description;
        this.baseFee = baseFee;
        this.feePerKg = feePerKg;
        this.freeAbove = freeAbove;
        this.minDays = minDays;
        this.maxDays = maxDays;
        this.sortOrder = sortOrder;
    }

    public Long getId() { return id; }
    public String getCode() { return code; }
    public String getName() { return name; }
    public String getCarrier() { return carrier; }
    public String getDescription() { return description; }
    public BigDecimal getBaseFee() { return baseFee; }
    public BigDecimal getFeePerKg() { return feePerKg; }
    public BigDecimal getFreeAbove() { return freeAbove; }
    public int getMinDays() { return minDays; }
    public int getMaxDays() { return maxDays; }
    public int getSortOrder() { return sortOrder; }
    public boolean isActive() { return Boolean.TRUE.equals(active); }
}
