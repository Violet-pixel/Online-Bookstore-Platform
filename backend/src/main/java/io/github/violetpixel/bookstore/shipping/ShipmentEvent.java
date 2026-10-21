package io.github.violetpixel.bookstore.shipping;

import java.time.Instant;

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

@Entity
@Table(name = "shipment_event")
public class ShipmentEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "shipment_id", nullable = false)
    private Shipment shipment;

    @Enumerated(EnumType.STRING)
    @Column(name = "shipment_status", nullable = false, length = 20)
    private ShipmentStatus status;

    @Column(nullable = false, length = 120)
    private String title;

    @Column(length = 300)
    private String detail;

    @Column(length = 120)
    private String location;

    @Column(nullable = false)
    private Instant occurredAt;

    protected ShipmentEvent() { }

    ShipmentEvent(Shipment shipment, ShipmentStatus status, String title, String detail, String location, Instant occurredAt) {
        this.shipment = shipment;
        this.status = status;
        this.title = title;
        this.detail = detail;
        this.location = location;
        this.occurredAt = occurredAt;
    }

    public Long getId() { return id; }
    public ShipmentStatus getStatus() { return status; }
    public String getTitle() { return title; }
    public String getDetail() { return detail; }
    public String getLocation() { return location; }
    public Instant getOccurredAt() { return occurredAt; }
}
