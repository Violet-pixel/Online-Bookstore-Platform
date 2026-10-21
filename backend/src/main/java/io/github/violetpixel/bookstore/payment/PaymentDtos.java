package io.github.violetpixel.bookstore.payment;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public final class PaymentDtos {

    private PaymentDtos() { }

    /** Dados do cartão: só passam pela memória e viram um token temporário no Redis. */
    public record CardRequest(
            @NotBlank(message = "Informe o número do cartão.") @Pattern(regexp = "[0-9 ]{12,23}", message = "Número de cartão inválido.") String number,
            @NotBlank(message = "Informe o nome impresso no cartão.") @Size(max = 60) String holderName,
            @Min(value = 1, message = "Mês inválido.") @Max(value = 12, message = "Mês inválido.") int expMonth,
            @Min(value = 2000, message = "Ano inválido.") @Max(value = 2100, message = "Ano inválido.") int expYear,
            @NotBlank(message = "Informe o código de segurança.") @Pattern(regexp = "\\d{3,4}", message = "Código de segurança inválido.") String cvv) {

        @Override
        public String toString() {
            return "CardRequest[oculto]";
        }
    }

    public record CardToken(String token, String brand, String last4, Instant expiresAt) {
    }

    public record PayRequest(@NotNull PaymentMethod method, String cardToken, @Min(1) @Max(12) Integer installments) {
    }

    public record Installment(int count, BigDecimal value, BigDecimal total, boolean interestFree) {
    }

    public record MethodInfo(String code, String name, String description, boolean instant) {
    }

    public record MethodsDto(List<MethodInfo> methods, int maxInstallments, BigDecimal minInstallmentValue, boolean sandbox,
            List<String> testCards) {
    }

    public record PaymentDto(String reference, String method, String status, BigDecimal amount, int installments,
            String cardBrand, String cardLast4, String pixPayload, String boletoLine, Instant expiresAt, Instant createdAt,
            Instant confirmedAt, String failureReason, String orderCode, String orderStatus) {

        public static PaymentDto of(Payment p) {
            return new PaymentDto(p.getGatewayReference(), p.getMethod().name(), p.getStatus().name(), p.getAmount(),
                    p.getInstallments(), p.getCardBrand(), p.getCardLast4(), p.getPixPayload(), p.getBoletoLine(),
                    p.getExpiresAt(), p.getCreatedAt(), p.getConfirmedAt(), p.getFailureReason(), p.getOrder().getCode(),
                    p.getOrder().getStatus().name());
        }
    }
}
