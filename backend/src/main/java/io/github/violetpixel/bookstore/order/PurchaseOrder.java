package io.github.violetpixel.bookstore.order;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

@Entity
@Table(name = "purchase_order")
public class PurchaseOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String code;

    @Column(nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "order_status", nullable = false, length = 20)
    private OrderStatus status = OrderStatus.PENDING_PAYMENT;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotal;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal discount;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal shippingFee;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal total;

    @Column(length = 30)
    private String couponCode;

    @Column(nullable = false)
    private Integer itemCount;

    @Embedded
    private ShippingAddress shipTo;

    @Column(nullable = false, length = 20)
    private String shippingMethodCode;

    @Column(nullable = false, length = 60)
    private String shippingMethodName;

    @Column(nullable = false, length = 60)
    private String shippingCarrier;

    @Column(nullable = false)
    private Integer shippingMinDays;

    @Column(nullable = false)
    private Integer shippingMaxDays;

    @Column(nullable = false)
    private Instant placedAt;

    @Column(nullable = false)
    private Instant paymentDueAt;

    private Instant paidAt;

    private Instant canceledAt;

    @Column(length = 200)
    private String cancelReason;

    @Version
    @Column(nullable = false)
    private Long version;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC")
    private List<OrderItem> items = new ArrayList<>();

    protected PurchaseOrder() { }

    public PurchaseOrder(String code, Long userId, ShippingAddress shipTo, Instant placedAt, Instant paymentDueAt) {
        this.code = code;
        this.userId = userId;
        this.shipTo = shipTo;
        this.placedAt = placedAt;
        this.paymentDueAt = paymentDueAt;
    }

    public boolean isAwaitingPayment() {
        return status == OrderStatus.PENDING_PAYMENT;
    }

    public Long getId() { return id; }
    public String getCode() { return code; }
    public Long getUserId() { return userId; }
    public OrderStatus getStatus() { return status; }
    public void setStatus(OrderStatus status) { this.status = status; }
    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }
    public BigDecimal getDiscount() { return discount; }
    public void setDiscount(BigDecimal discount) { this.discount = discount; }
    public BigDecimal getShippingFee() { return shippingFee; }
    public void setShippingFee(BigDecimal shippingFee) { this.shippingFee = shippingFee; }
    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }
    public String getCouponCode() { return couponCode; }
    public void setCouponCode(String couponCode) { this.couponCode = couponCode; }
    public int getItemCount() { return itemCount; }
    public void setItemCount(int itemCount) { this.itemCount = itemCount; }
    public ShippingAddress getShipTo() { return shipTo; }
    public String getShippingMethodCode() { return shippingMethodCode; }
    public String getShippingMethodName() { return shippingMethodName; }
    public String getShippingCarrier() { return shippingCarrier; }
    public int getShippingMinDays() { return shippingMinDays; }
    public int getShippingMaxDays() { return shippingMaxDays; }
    public void setShipping(String code, String name, String carrier, int minDays, int maxDays) {
        this.shippingMethodCode = code;
        this.shippingMethodName = name;
        this.shippingCarrier = carrier;
        this.shippingMinDays = minDays;
        this.shippingMaxDays = maxDays;
    }
    public Instant getPlacedAt() { return placedAt; }
    public Instant getPaymentDueAt() { return paymentDueAt; }
    public void setPaymentDueAt(Instant paymentDueAt) { this.paymentDueAt = paymentDueAt; }
    public Instant getPaidAt() { return paidAt; }
    public void setPaidAt(Instant paidAt) { this.paidAt = paidAt; }
    public Instant getCanceledAt() { return canceledAt; }
    public void setCanceledAt(Instant canceledAt) { this.canceledAt = canceledAt; }
    public String getCancelReason() { return cancelReason; }
    public void setCancelReason(String cancelReason) { this.cancelReason = cancelReason; }
    public List<OrderItem> getItems() { return items; }
}
