package io.github.violetpixel.bookstore.order;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import io.github.violetpixel.bookstore.payment.PaymentDtos.PaymentDto;
import io.github.violetpixel.bookstore.shipping.ShippingQuoteService.ShippingOption;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public final class OrderDtos {

    private OrderDtos() { }

    public record QuoteRequest(Long addressId, String zipCode, @Size(max = 30) String couponCode) {
    }

    public record QuoteDto(int itemCount, int weightGrams, BigDecimal subtotal, BigDecimal discount, String couponCode,
            boolean couponValid, String couponMessage, List<ShippingOption> shippingOptions) {
    }

    public record CheckoutRequest(@NotNull(message = "Escolha um endereço.") Long addressId,
            @NotBlank(message = "Escolha uma forma de envio.") String shippingMethod,
            @Size(max = 30) String couponCode) {
    }

    public record CancelRequest(@Size(max = 200) String reason) {
    }

    public record OrderItemDto(long bookId, String slug, String title, String authors, int coverStyle, BigDecimal unitPrice,
            int quantity, BigDecimal lineTotal) {
        static OrderItemDto of(OrderItem i) {
            return new OrderItemDto(i.getBookId(), i.getBookSlug(), i.getBookTitle(), i.getBookAuthors(), i.getCoverStyle(),
                    i.getUnitPrice(), i.getQuantity(), i.getLineTotal());
        }
    }

    public record ShipToDto(String recipient, String zipCode, String street, String number, String complement,
            String district, String city, String state) {
        static ShipToDto of(ShippingAddress a) {
            return new ShipToDto(a.getRecipient(), a.getZipCode(), a.getStreet(), a.getNumber(), a.getComplement(),
                    a.getDistrict(), a.getCity(), a.getState());
        }
    }

    public record ShippingDto(String code, String name, String carrier, int minDays, int maxDays) {
    }

    public record ShipmentEventDto(String status, String title, String detail, String location, Instant occurredAt) {
    }

    public record ShipmentDto(String carrier, String serviceName, String trackingCode, String status,
            LocalDate estimatedDelivery, Instant shippedAt, Instant deliveredAt, List<ShipmentEventDto> events) {
    }

    public record CustomerDto(long id, String fullName, String email) {
    }

    public record OrderSummaryDto(String code, String status, Instant placedAt, Instant paymentDueAt, BigDecimal total,
            int itemCount, List<OrderItemDto> preview, String shipmentStatus, LocalDate estimatedDelivery,
            String paymentMethod, String customerName) {
    }

    public record OrderDetailDto(String code, String status, Instant placedAt, Instant paymentDueAt, Instant paidAt,
            Instant canceledAt, String cancelReason, BigDecimal subtotal, BigDecimal discount, BigDecimal shippingFee,
            BigDecimal total, String couponCode, int itemCount, List<OrderItemDto> items, ShipToDto shipTo,
            ShippingDto shipping, List<PaymentDto> payments, ShipmentDto shipment, boolean canPay, boolean canCancel,
            CustomerDto customer) {
    }

    public record AdvanceRequest(String status, @Size(max = 30) String trackingCode, @Size(max = 300) String note,
            @Size(max = 120) String location) {
    }
}
