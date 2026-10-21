package io.github.violetpixel.bookstore.shipping;

public enum ShipmentStatus {
    PREPARING,
    IN_TRANSIT,
    OUT_FOR_DELIVERY,
    DELIVERED,
    RETURNED;

    public ShipmentStatus next() {
        return switch (this) {
            case PREPARING -> IN_TRANSIT;
            case IN_TRANSIT -> OUT_FOR_DELIVERY;
            case OUT_FOR_DELIVERY -> DELIVERED;
            default -> null;
        };
    }
}
