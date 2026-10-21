package io.github.violetpixel.bookstore.payment;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import io.github.violetpixel.bookstore.auth.CurrentUser;
import io.github.violetpixel.bookstore.payment.PaymentDtos.CardRequest;
import io.github.violetpixel.bookstore.payment.PaymentDtos.CardToken;
import io.github.violetpixel.bookstore.payment.PaymentDtos.Installment;
import io.github.violetpixel.bookstore.payment.PaymentDtos.MethodsDto;
import io.github.violetpixel.bookstore.payment.PaymentDtos.PayRequest;
import io.github.violetpixel.bookstore.payment.PaymentDtos.PaymentDto;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api")
@Tag(name = "Pagamentos")
public class PaymentController {

    private final PaymentService payments;
    private final CardTokenService cardTokens;

    public PaymentController(PaymentService payments, CardTokenService cardTokens) {
        this.payments = payments;
        this.cardTokens = cardTokens;
    }

    @GetMapping("/payments/methods")
    public MethodsDto methods() {
        return payments.methods();
    }

    /** Troca os dados do cartão por um token de uso único (10 min). O número nunca é gravado. */
    @PostMapping("/payments/card-tokens")
    @ResponseStatus(HttpStatus.CREATED)
    public CardToken tokenize(Authentication auth, @Valid @RequestBody CardRequest card) {
        return cardTokens.tokenize(CurrentUser.id(auth), card);
    }

    @GetMapping("/orders/{code}/installments")
    public List<Installment> installments(Authentication auth, @PathVariable String code) {
        return payments.installmentsFor(CurrentUser.id(auth), code);
    }

    @PostMapping("/orders/{code}/payments")
    @ResponseStatus(HttpStatus.CREATED)
    public PaymentDto pay(Authentication auth, @PathVariable String code, @Valid @RequestBody PayRequest req,
            @RequestHeader(name = "Idempotency-Key", required = false) String idempotencyKey) {
        return payments.pay(CurrentUser.id(auth), code, req, idempotencyKey);
    }

    @GetMapping("/payments/{reference}")
    public PaymentDto status(Authentication auth, @PathVariable String reference) {
        return payments.status(CurrentUser.id(auth), reference);
    }

    /** Só existe com PAYMENTS_SANDBOX=true: faz o papel do banco confirmando Pix e boleto. */
    @PostMapping("/sandbox/payments/{reference}/confirm")
    public PaymentDto sandboxConfirm(Authentication auth, @PathVariable String reference) {
        return payments.sandboxConfirm(CurrentUser.id(auth), CurrentUser.isAdmin(auth), reference);
    }
}
