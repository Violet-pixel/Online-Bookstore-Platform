package io.github.violetpixel.bookstore.order;

import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import io.github.violetpixel.bookstore.auth.UserRepository;
import io.github.violetpixel.bookstore.order.OrderDtos.CustomerDto;
import io.github.violetpixel.bookstore.order.OrderDtos.OrderDetailDto;
import io.github.violetpixel.bookstore.order.OrderDtos.OrderItemDto;
import io.github.violetpixel.bookstore.order.OrderDtos.OrderSummaryDto;
import io.github.violetpixel.bookstore.order.OrderDtos.ShipToDto;
import io.github.violetpixel.bookstore.order.OrderDtos.ShipmentDto;
import io.github.violetpixel.bookstore.order.OrderDtos.ShipmentEventDto;
import io.github.violetpixel.bookstore.order.OrderDtos.ShippingDto;
import io.github.violetpixel.bookstore.payment.Payment;
import io.github.violetpixel.bookstore.payment.PaymentDtos.PaymentDto;
import io.github.violetpixel.bookstore.payment.PaymentRepository;
import io.github.violetpixel.bookstore.shipping.Shipment;
import io.github.violetpixel.bookstore.shipping.ShipmentRepository;
import io.github.violetpixel.bookstore.shipping.ShipmentStatus;

@Component
public class OrderMapper {

    private final PaymentRepository payments;
    private final ShipmentRepository shipments;
    private final UserRepository users;

    public OrderMapper(PaymentRepository payments, ShipmentRepository shipments, UserRepository users) {
        this.payments = payments;
        this.shipments = shipments;
        this.users = users;
    }

    @Transactional(readOnly = true)
    public OrderSummaryDto summary(PurchaseOrder o, boolean withCustomer) {
        Shipment s = shipments.findByOrderId(o.getId()).orElse(null);
        List<Payment> list = payments.findByOrderIdOrderByCreatedAtAscIdAsc(o.getId());
        String method = list.isEmpty() ? null : list.get(list.size() - 1).getMethod().name();
        String customer = withCustomer ? users.findById(o.getUserId()).map(u -> u.getFullName()).orElse(null) : null;
        return new OrderSummaryDto(o.getCode(), o.getStatus().name(), o.getPlacedAt(), o.getPaymentDueAt(), o.getTotal(),
                o.getItemCount(), o.getItems().stream().limit(4).map(OrderItemDto::of).toList(),
                s == null ? null : s.getStatus().name(), s == null ? null : s.getEstimatedDelivery(), method, customer);
    }

    @Transactional(readOnly = true)
    public OrderDetailDto detail(PurchaseOrder o, boolean withCustomer) {
        List<PaymentDto> paymentDtos = payments.findByOrderIdOrderByCreatedAtAscIdAsc(o.getId()).stream()
            .map(PaymentDto::of).toList();
        Shipment s = shipments.findByOrderId(o.getId()).orElse(null);
        ShipmentDto shipment = s == null ? null : new ShipmentDto(s.getCarrier(), s.getServiceName(), s.getTrackingCode(),
                s.getStatus().name(), s.getEstimatedDelivery(), s.getShippedAt(), s.getDeliveredAt(),
                s.getEvents().stream().map(e -> new ShipmentEventDto(e.getStatus().name(), e.getTitle(), e.getDetail(),
                        e.getLocation(), e.getOccurredAt())).toList());
        boolean canCancel = o.getStatus() == OrderStatus.PENDING_PAYMENT
                || (o.getStatus() == OrderStatus.PAID && (s == null || s.getStatus() == ShipmentStatus.PREPARING));
        CustomerDto customer = withCustomer
                ? users.findById(o.getUserId()).map(u -> new CustomerDto(u.getId(), u.getFullName(), u.getEmail())).orElse(null)
                : null;
        return new OrderDetailDto(o.getCode(), o.getStatus().name(), o.getPlacedAt(), o.getPaymentDueAt(), o.getPaidAt(),
                o.getCanceledAt(), o.getCancelReason(), o.getSubtotal(), o.getDiscount(), o.getShippingFee(), o.getTotal(),
                o.getCouponCode(), o.getItemCount(), o.getItems().stream().map(OrderItemDto::of).toList(),
                ShipToDto.of(o.getShipTo()),
                new ShippingDto(o.getShippingMethodCode(), o.getShippingMethodName(), o.getShippingCarrier(),
                        o.getShippingMinDays(), o.getShippingMaxDays()),
                paymentDtos, shipment, o.isAwaitingPayment(), canCancel, customer);
    }
}
