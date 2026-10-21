package io.github.violetpixel.bookstore.payment;

import java.math.BigDecimal;
import java.time.Instant;

import io.github.violetpixel.bookstore.order.PurchaseOrder;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * Tentativa de pagamento de um pedido. Nunca guarda número de cartão nem CVV: só a bandeira e os 4 últimos dígitos.
 */
@Entity
@Table(name = "payment")
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private PurchaseOrder order;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false, length = 20)
    private PaymentMethod method;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false, length = 20)
    private PaymentStatus status = PaymentStatus.PENDING;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false)
    private Integer installments = 1;

    @Column(length = 20)
    private String cardBrand;

    @Column(length = 4)
    private String cardLast4;

    @Column(length = 400)
    private String pixPayload;

    @Column(length = 60)
    private String boletoLine;

    @Column(nullable = false, unique = true, length = 40)
    private String gatewayReference;

    @Column(nullable = false, unique = true, length = 80)
    private String idempotencyKey;

    @Column(length = 200)
    private String failureReason;

    private Instant expiresAt;

    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    private Instant confirmedAt;

    protected Payment() { }

    public Payment(PurchaseOrder order, PaymentMethod method, BigDecimal amount, String gatewayReference, String idempotencyKey) {
        this.order = order;
        this.method = method;
        this.amount = amount;
        this.gatewayReference = gatewayReference;
        this.idempotencyKey = idempotencyKey;
    }

    public Long getId() { return id; }
    public PurchaseOrder getOrder() { return order; }
    public PaymentMethod getMethod() { return method; }
    public PaymentStatus getStatus() { return status; }
    public void setStatus(PaymentStatus status) { this.status = status; }
    public BigDecimal getAmount() { return amount; }
    public int getInstallments() { return installments; }
    public void setInstallments(int installments) { this.installments = installments; }
    public String getCardBrand() { return cardBrand; }
    public void setCardBrand(String cardBrand) { this.cardBrand = cardBrand; }
    public String getCardLast4() { return cardLast4; }
    public void setCardLast4(String cardLast4) { this.cardLast4 = cardLast4; }
    public String getPixPayload() { return pixPayload; }
    public void setPixPayload(String pixPayload) { this.pixPayload = pixPayload; }
    public String getBoletoLine() { return boletoLine; }
    public void setBoletoLine(String boletoLine) { this.boletoLine = boletoLine; }
    public String getGatewayReference() { return gatewayReference; }
    public String getIdempotencyKey() { return idempotencyKey; }
    public String getFailureReason() { return failureReason; }
    public void setFailureReason(String failureReason) { this.failureReason = failureReason; }
    public Instant getExpiresAt() { return expiresAt; }
    public void setExpiresAt(Instant expiresAt) { this.expiresAt = expiresAt; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getConfirmedAt() { return confirmedAt; }
    public void setConfirmedAt(Instant confirmedAt) { this.confirmedAt = confirmedAt; }
}
