package io.github.violetpixel.bookstore.shipping;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import io.github.violetpixel.bookstore.order.PurchaseOrder;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

@Entity
@Table(name = "shipment")
public class Shipment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false, unique = true)
    private PurchaseOrder order;

    @Column(nullable = false, length = 60)
    private String carrier;

    @Column(nullable = false, length = 60)
    private String serviceName;

    @Column(length = 30)
    private String trackingCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "shipment_status", nullable = false, length = 20)
    private ShipmentStatus status = ShipmentStatus.PREPARING;

    private LocalDate estimatedDelivery;

    private Instant shippedAt;

    private Instant deliveredAt;

    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    @Column(nullable = false)
    private Instant updatedAt = Instant.now();

    @OneToMany(mappedBy = "shipment", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("occurredAt ASC, id ASC")
    private List<ShipmentEvent> events = new ArrayList<>();

    protected Shipment() { }

    public Shipment(PurchaseOrder order, String carrier, String serviceName, LocalDate estimatedDelivery) {
        this.order = order;
        this.carrier = carrier;
        this.serviceName = serviceName;
        this.estimatedDelivery = estimatedDelivery;
    }

    public ShipmentEvent addEvent(ShipmentStatus status, String title, String detail, String location, Instant at) {
        ShipmentEvent event = new ShipmentEvent(this, status, title, detail, location, at);
        events.add(event);
        this.status = status;
        this.updatedAt = at;
        return event;
    }

    public Long getId() { return id; }
    public PurchaseOrder getOrder() { return order; }
    public String getCarrier() { return carrier; }
    public String getServiceName() { return serviceName; }
    public String getTrackingCode() { return trackingCode; }
    public void setTrackingCode(String trackingCode) { this.trackingCode = trackingCode; }
    public ShipmentStatus getStatus() { return status; }
    public LocalDate getEstimatedDelivery() { return estimatedDelivery; }
    public void setEstimatedDelivery(LocalDate estimatedDelivery) { this.estimatedDelivery = estimatedDelivery; }
    public Instant getShippedAt() { return shippedAt; }
    public void setShippedAt(Instant shippedAt) { this.shippedAt = shippedAt; }
    public Instant getDeliveredAt() { return deliveredAt; }
    public void setDeliveredAt(Instant deliveredAt) { this.deliveredAt = deliveredAt; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public List<ShipmentEvent> getEvents() { return events; }
}
