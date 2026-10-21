package io.github.violetpixel.bookstore.order;

import java.security.SecureRandom;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Locale;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.github.violetpixel.bookstore.catalog.BookRepository;
import io.github.violetpixel.bookstore.catalog.CatalogCache;
import io.github.violetpixel.bookstore.common.ApiException;
import io.github.violetpixel.bookstore.order.OrderDtos.AdvanceRequest;
import io.github.violetpixel.bookstore.payment.Payment;
import io.github.violetpixel.bookstore.payment.PaymentRepository;
import io.github.violetpixel.bookstore.payment.PaymentStatus;
import io.github.violetpixel.bookstore.shipping.Shipment;
import io.github.violetpixel.bookstore.shipping.ShipmentRepository;
import io.github.violetpixel.bookstore.shipping.ShipmentStatus;

/** Ciclo de vida do pedido depois do checkout: pagamento confirmado, envio, entrega e cancelamento. */
@Service
public class FulfillmentService {

    static final ZoneId BRT = ZoneId.of("America/Sao_Paulo");
    private static final String HUB = "Centro de distribuição · Rio de Janeiro, RJ";

    private final ShipmentRepository shipments;
    private final PaymentRepository payments;
    private final BookRepository books;
    private final CatalogCache cache;
    private final SecureRandom random = new SecureRandom();

    public FulfillmentService(ShipmentRepository shipments, PaymentRepository payments, BookRepository books, CatalogCache cache) {
        this.shipments = shipments;
        this.payments = payments;
        this.books = books;
        this.cache = cache;
    }

    @Transactional
    public Shipment markPaid(PurchaseOrder order, Instant at) {
        if (order.getStatus() != OrderStatus.PENDING_PAYMENT) {
            throw ApiException.conflict("pedido_nao_pendente", "Este pedido não está aguardando pagamento.");
        }
        order.setStatus(OrderStatus.PAID);
        order.setPaidAt(at);
        Shipment shipment = new Shipment(order, order.getShippingCarrier(), order.getShippingMethodName(),
                businessDaysAfter(at, order.getShippingMaxDays()));
        shipment.setCreatedAt(at);
        shipment.addEvent(ShipmentStatus.PREPARING, "Pagamento confirmado", "Recebemos o pagamento do pedido " + order.getCode() + ".",
                null, at);
        shipment.addEvent(ShipmentStatus.PREPARING, "Pedido em separação", "Os livros estão sendo separados e embalados.", HUB,
                at.plusSeconds(1));
        return shipments.save(shipment);
    }

    @Transactional
    public Shipment advance(PurchaseOrder order, AdvanceRequest req, Instant at) {
        Shipment s = shipments.findByOrderId(order.getId())
            .orElseThrow(() -> ApiException.unprocessable("sem_envio", "O pedido ainda não foi pago."));
        ShipmentStatus target = req == null || req.status() == null || req.status().isBlank() ? s.getStatus().next()
                : parse(req.status());
        if (target == null || target.ordinal() <= s.getStatus().ordinal() || target == ShipmentStatus.RETURNED) {
            throw ApiException.unprocessable("etapa_invalida", "Este envio não pode ir para essa etapa.");
        }
        String note = req == null ? null : blankToNull(req.note());
        String location = req == null ? null : blankToNull(req.location());
        if (target.ordinal() >= ShipmentStatus.IN_TRANSIT.ordinal() && s.getTrackingCode() == null) {
            String code = req != null && blankToNull(req.trackingCode()) != null ? req.trackingCode().trim().toUpperCase(Locale.ROOT)
                    : trackingCode();
            s.setTrackingCode(code);
            s.setShippedAt(at);
            order.setStatus(OrderStatus.SHIPPED);
            s.addEvent(ShipmentStatus.IN_TRANSIT, "Pedido enviado",
                    note != null ? note : "Objeto postado com o código " + code + ".", location != null ? location : HUB, at);
        }
        String city = order.getShipTo().getCity() + ", " + order.getShipTo().getState();
        if (target.ordinal() >= ShipmentStatus.OUT_FOR_DELIVERY.ordinal()
                && s.getStatus().ordinal() < ShipmentStatus.OUT_FOR_DELIVERY.ordinal()) {
            s.addEvent(ShipmentStatus.OUT_FOR_DELIVERY, "Saiu para entrega",
                    target == ShipmentStatus.OUT_FOR_DELIVERY && note != null ? note : "O entregador está a caminho do seu endereço.",
                    city, at.plusSeconds(1));
        }
        if (target == ShipmentStatus.DELIVERED) {
            s.setDeliveredAt(at.plusSeconds(2));
            order.setStatus(OrderStatus.DELIVERED);
            s.addEvent(ShipmentStatus.DELIVERED, "Pedido entregue",
                    note != null ? note : "Entregue para " + order.getShipTo().getRecipient() + ". Boa leitura!", city,
                    at.plusSeconds(2));
        }
        return s;
    }

    @Transactional
    public void cancel(PurchaseOrder order, String reason, Instant at) {
        if (order.getStatus() == OrderStatus.CANCELED) {
            return;
        }
        Shipment s = shipments.findByOrderId(order.getId()).orElse(null);
        boolean cancellable = order.getStatus() == OrderStatus.PENDING_PAYMENT
                || (order.getStatus() == OrderStatus.PAID && (s == null || s.getStatus() == ShipmentStatus.PREPARING));
        if (!cancellable) {
            throw ApiException.unprocessable("nao_cancelavel", "O pedido já foi enviado e não pode mais ser cancelado.");
        }
        for (Payment p : payments.findByOrderIdOrderByCreatedAtAscIdAsc(order.getId())) {
            if (p.getStatus() == PaymentStatus.PENDING) {
                p.setStatus(PaymentStatus.CANCELED);
            } else if (p.getStatus() == PaymentStatus.APPROVED) {
                p.setStatus(PaymentStatus.REFUNDED);
            }
        }
        if (s != null) {
            s.addEvent(ShipmentStatus.RETURNED, "Pedido cancelado", "O envio foi interrompido e os livros voltaram ao estoque.", HUB, at);
        }
        for (OrderItem item : order.getItems()) {
            books.release(item.getBookId(), item.getQuantity());
        }
        order.setStatus(OrderStatus.CANCELED);
        order.setCanceledAt(at);
        order.setCancelReason(reason);
        cache.evictBooks(order.getItems().stream().map(OrderItem::getBookSlug).toList());
    }

    static LocalDate businessDaysAfter(Instant start, int days) {
        LocalDate date = start.atZone(BRT).toLocalDate();
        int added = 0;
        while (added < days) {
            date = date.plusDays(1);
            if (date.getDayOfWeek() != DayOfWeek.SATURDAY && date.getDayOfWeek() != DayOfWeek.SUNDAY) {
                added++;
            }
        }
        return date;
    }

    private String trackingCode() {
        StringBuilder sb = new StringBuilder("LV");
        for (int i = 0; i < 9; i++) {
            sb.append(random.nextInt(10));
        }
        return sb.append("BR").toString();
    }

    private static ShipmentStatus parse(String value) {
        try {
            return ShipmentStatus.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw ApiException.badRequest("etapa_invalida", "Etapa de envio desconhecida.");
        }
    }

    private static String blankToNull(String v) {
        return v == null || v.isBlank() ? null : v.trim();
    }
}
