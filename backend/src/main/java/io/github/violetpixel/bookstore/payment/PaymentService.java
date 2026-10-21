package io.github.violetpixel.bookstore.payment;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.github.violetpixel.bookstore.common.ApiException;
import io.github.violetpixel.bookstore.config.AppProperties;
import io.github.violetpixel.bookstore.order.FulfillmentService;
import io.github.violetpixel.bookstore.order.OrderRepository;
import io.github.violetpixel.bookstore.order.OrderStatus;
import io.github.violetpixel.bookstore.order.PurchaseOrder;
import io.github.violetpixel.bookstore.payment.CardTokenService.TokenData;
import io.github.violetpixel.bookstore.payment.PaymentDtos.Installment;
import io.github.violetpixel.bookstore.payment.PaymentDtos.MethodInfo;
import io.github.violetpixel.bookstore.payment.PaymentDtos.MethodsDto;
import io.github.violetpixel.bookstore.payment.PaymentDtos.PayRequest;
import io.github.violetpixel.bookstore.payment.PaymentDtos.PaymentDto;
import io.github.violetpixel.bookstore.payment.PaymentGateway.Authorization;

@Service
public class PaymentService {

    private static final ZoneId BRT = ZoneId.of("America/Sao_Paulo");

    private final PaymentRepository payments;
    private final OrderRepository orders;
    private final CardTokenService cardTokens;
    private final PaymentGateway gateway;
    private final FulfillmentService fulfillment;
    private final AppProperties props;

    public PaymentService(PaymentRepository payments, OrderRepository orders, CardTokenService cardTokens, PaymentGateway gateway,
            FulfillmentService fulfillment, AppProperties props) {
        this.payments = payments;
        this.orders = orders;
        this.cardTokens = cardTokens;
        this.gateway = gateway;
        this.fulfillment = fulfillment;
        this.props = props;
    }

    public MethodsDto methods() {
        List<MethodInfo> list = List.of(
                new MethodInfo("CREDIT_CARD", "Cartão de crédito", "Em até " + props.payments().maxInstallments() + "x sem juros", true),
                new MethodInfo("PIX", "Pix", "Aprovação na hora com o QR Code ou o copia e cola", true),
                new MethodInfo("BOLETO", "Boleto bancário", "Vence em " + props.payments().boletoDays() + " dias úteis; compensação em até 2 dias", false));
        List<String> testCards = props.payments().sandbox()
                ? List.of("4111 1111 1111 1111 · aprova", "5555 5555 5555 4444 · aprova", "4000 0000 0000 0002 · recusa",
                        "4000 0000 0000 9995 · limite insuficiente")
                : List.of();
        return new MethodsDto(list, props.payments().maxInstallments(), props.payments().minInstallmentValue(),
                props.payments().sandbox(), testCards);
    }

    public List<Installment> installments(BigDecimal total) {
        int max = Math.max(1, Math.min(props.payments().maxInstallments(),
                total.divide(props.payments().minInstallmentValue(), 0, RoundingMode.FLOOR).intValue()));
        List<Installment> list = new ArrayList<>();
        for (int n = 1; n <= max; n++) {
            list.add(new Installment(n, total.divide(BigDecimal.valueOf(n), 2, RoundingMode.CEILING), total, true));
        }
        return list;
    }

    @Transactional(readOnly = true)
    public List<Installment> installmentsFor(long userId, String orderCode) {
        return installments(own(userId, orderCode).getTotal());
    }

    @Transactional
    public PaymentDto pay(long userId, String orderCode, PayRequest req, String idempotencyKey) {
        String key = "u" + userId + ":" + (idempotencyKey == null || idempotencyKey.isBlank() ? UUID.randomUUID()
                : idempotencyKey.trim());
        if (key.length() > 80) {
            throw ApiException.badRequest("chave_invalida", "Idempotency-Key muito longa.");
        }
        Payment previous = payments.findByIdempotencyKey(key).orElse(null);
        if (previous != null) {
            return PaymentDto.of(previous);
        }
        PurchaseOrder order = own(userId, orderCode);
        if (order.getStatus() != OrderStatus.PENDING_PAYMENT) {
            throw ApiException.conflict("pedido_nao_pendente", "Este pedido não está aguardando pagamento.");
        }
        Instant now = Instant.now();
        if (order.getPaymentDueAt().isBefore(now)) {
            throw ApiException.unprocessable("prazo_expirado", "O prazo para pagar este pedido terminou.");
        }

        if (req.method() != PaymentMethod.CREDIT_CARD) {
            // Pix e boleto pendentes convivem; reaproveita o do mesmo tipo se ainda estiver valendo
            for (Payment p : payments.findByOrderIdAndStatus(order.getId(), PaymentStatus.PENDING)) {
                if (p.getMethod() == req.method()) {
                    if (p.getExpiresAt() == null || p.getExpiresAt().isAfter(now)) {
                        return PaymentDto.of(p);
                    }
                    p.setStatus(PaymentStatus.EXPIRED);
                }
            }
        }

        Payment payment = new Payment(order, req.method(), order.getTotal(), gateway.newReference(), key);
        switch (req.method()) {
            case CREDIT_CARD -> payCard(userId, order, payment, req, now);
            case PIX -> {
                Instant expires = min(now.plus(props.payments().pixTtl()), order.getPaymentDueAt());
                payment.setExpiresAt(expires);
                payment.setPixPayload(gateway.pixPayload(payment.getGatewayReference(), payment.getAmount(), expires));
            }
            case BOLETO -> {
                LocalDate due = businessDays(now, props.payments().boletoDays());
                Instant expires = due.plusDays(1).atStartOfDay(BRT).toInstant();
                payment.setExpiresAt(expires);
                payment.setBoletoLine(gateway.boletoLine(payment.getGatewayReference(), payment.getAmount(), due));
                if (order.getPaymentDueAt().isBefore(expires)) {
                    order.setPaymentDueAt(expires);
                }
            }
        }
        payments.save(payment);
        return PaymentDto.of(payment);
    }

    private void payCard(long userId, PurchaseOrder order, Payment payment, PayRequest req, Instant now) {
        TokenData card = cardTokens.consume(req.cardToken(), userId)
            .orElseThrow(() -> ApiException.unprocessable("token_invalido", "Os dados do cartão expiraram. Digite de novo."));
        int installments = req.installments() == null ? 1 : req.installments();
        int max = installments(order.getTotal()).size();
        if (installments < 1 || installments > max) {
            throw ApiException.badRequest("parcelas_invalidas", "Escolha de 1 a " + max + " parcelas.");
        }
        payment.setInstallments(installments);
        payment.setCardBrand(card.brand());
        payment.setCardLast4(card.last4());
        Authorization auth = gateway.authorizeCard(card, payment.getAmount(), installments);
        if (auth.approved()) {
            payment.setStatus(PaymentStatus.APPROVED);
            payment.setConfirmedAt(now);
            cancelOtherPending(order, null);
            fulfillment.markPaid(order, now);
        } else {
            payment.setStatus(PaymentStatus.DECLINED);
            payment.setFailureReason(auth.reason());
        }
    }

    @Transactional(readOnly = true)
    public PaymentDto status(long userId, String reference) {
        Payment p = payments.findByGatewayReference(reference).orElseThrow(() -> ApiException.notFound("Pagamento"));
        if (!p.getOrder().getUserId().equals(userId)) {
            throw ApiException.notFound("Pagamento");
        }
        return PaymentDto.of(p);
    }

    /** Sandbox: simula a confirmação do banco para Pix e boleto. */
    @Transactional
    public PaymentDto sandboxConfirm(long userId, boolean admin, String reference) {
        if (!props.payments().sandbox()) {
            throw ApiException.notFound("Recurso");
        }
        Payment p = payments.findByGatewayReference(reference).orElseThrow(() -> ApiException.notFound("Pagamento"));
        if (!admin && !p.getOrder().getUserId().equals(userId)) {
            throw ApiException.notFound("Pagamento");
        }
        if (p.getStatus() != PaymentStatus.PENDING) {
            throw ApiException.conflict("pagamento_nao_pendente", "Este pagamento não está pendente.");
        }
        Instant now = Instant.now();
        if (p.getExpiresAt() != null && p.getExpiresAt().isBefore(now)) {
            throw ApiException.unprocessable("pagamento_expirado", "Este código de pagamento expirou.");
        }
        p.setStatus(PaymentStatus.APPROVED);
        p.setConfirmedAt(now);
        cancelOtherPending(p.getOrder(), p);
        fulfillment.markPaid(p.getOrder(), now);
        return PaymentDto.of(p);
    }

    /** Quando um pagamento é aprovado, os outros códigos pendentes do pedido (Pix ou boleto) deixam de valer. */
    private void cancelOtherPending(PurchaseOrder order, Payment approved) {
        for (Payment other : payments.findByOrderIdAndStatus(order.getId(), PaymentStatus.PENDING)) {
            if (approved == null || !other.getId().equals(approved.getId())) {
                other.setStatus(PaymentStatus.CANCELED);
            }
        }
    }

    private PurchaseOrder own(long userId, String code) {
        return orders.findByCodeAndUserId(code, userId).orElseThrow(() -> ApiException.notFound("Pedido"));
    }

    private static Instant min(Instant a, Instant b) {
        return a.isBefore(b) ? a : b;
    }

    private static LocalDate businessDays(Instant from, int days) {
        LocalDate d = from.atZone(BRT).toLocalDate();
        int added = 0;
        while (added < days) {
            d = d.plusDays(1);
            if (d.getDayOfWeek().getValue() < 6) {
                added++;
            }
        }
        return d;
    }
}
