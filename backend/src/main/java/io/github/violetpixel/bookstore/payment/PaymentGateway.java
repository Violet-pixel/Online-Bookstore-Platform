package io.github.violetpixel.bookstore.payment;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

import io.github.violetpixel.bookstore.payment.CardTokenService.TokenData;

/** Contrato com o provedor de pagamentos. O projeto traz um sandbox; um provedor real implementa esta interface. */
public interface PaymentGateway {

    record Authorization(boolean approved, String reason) {
    }

    String newReference();

    Authorization authorizeCard(TokenData card, BigDecimal amount, int installments);

    String pixPayload(String reference, BigDecimal amount, Instant expiresAt);

    String boletoLine(String reference, BigDecimal amount, LocalDate dueDate);
}
