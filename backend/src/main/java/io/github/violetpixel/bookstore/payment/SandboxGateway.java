package io.github.violetpixel.bookstore.payment;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Locale;

import org.springframework.stereotype.Component;

import io.github.violetpixel.bookstore.payment.CardTokenService.TokenData;

/**
 * Provedor de pagamentos de demonstração. Nada sai da aplicação e nenhum código gerado aqui paga algo de verdade:
 * o "Pix copia e cola" usa um identificador de sandbox (não é um BR Code válido) e o boleto usa o banco fictício 000.
 *
 * <p>Cartões de teste: 4111 1111 1111 1111 e 5555 5555 5555 4444 aprovam; 4000 0000 0000 0002 é recusado;
 * 4000 0000 0000 9995 é recusado por limite. Qualquer outro número válido (Luhn) é aprovado.
 */
@Component
public class SandboxGateway implements PaymentGateway {

    private static final String ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private final SecureRandom random = new SecureRandom();

    static String outcomeFor(String digits) {
        return switch (digits) {
            case "4000000000000002" -> "DECLINED";
            case "4000000000009995" -> "INSUFFICIENT_FUNDS";
            default -> "APPROVED";
        };
    }

    @Override
    public String newReference() {
        StringBuilder sb = new StringBuilder("pay_");
        for (int i = 0; i < 20; i++) {
            sb.append(ALPHABET.charAt(random.nextInt(ALPHABET.length())));
        }
        return sb.toString().toLowerCase(Locale.ROOT);
    }

    @Override
    public Authorization authorizeCard(TokenData card, BigDecimal amount, int installments) {
        return switch (card.outcome()) {
            case "DECLINED" -> new Authorization(false, "Pagamento recusado pelo emissor do cartão.");
            case "INSUFFICIENT_FUNDS" -> new Authorization(false, "Limite insuficiente para esta compra.");
            default -> new Authorization(true, null);
        };
    }

    @Override
    public String pixPayload(String reference, BigDecimal amount, Instant expiresAt) {
        String txid = reference.replace("pay_", "").toUpperCase(Locale.ROOT);
        String merchant = field("00", "dev.sandbox.pix") + field("01", "sandbox-" + txid.toLowerCase(Locale.ROOT));
        String payload = field("00", "01")
                + field("26", merchant)
                + field("52", "0000")
                + field("53", "986")
                + field("54", amount.toPlainString())
                + field("58", "BR")
                + field("59", "LIVRARIA SANDBOX")
                + field("60", "RIO DE JANEIRO")
                + field("62", field("05", txid))
                + "6304";
        return payload + crc16(payload);
    }

    @Override
    public String boletoLine(String reference, BigDecimal amount, LocalDate dueDate) {
        long factor = ChronoUnit.DAYS.between(LocalDate.of(2025, 2, 22), dueDate) + 1000;
        String value = String.format("%010d", amount.movePointRight(2).longValueExact());
        StringBuilder free = new StringBuilder();
        for (char c : reference.toCharArray()) {
            if (free.length() < 25) {
                free.append(Character.isDigit(c) ? c : (char) ('0' + (c % 10)));
            }
        }
        while (free.length() < 25) {
            free.append('0');
        }
        String f = free.toString();
        String b1 = "0009" + f.substring(0, 5);
        String b2 = f.substring(5, 15);
        String b3 = f.substring(15, 25);
        return b1.substring(0, 5) + "." + b1.substring(5) + mod10(b1) + " "
                + b2.substring(0, 5) + "." + b2.substring(5) + mod10(b2) + " "
                + b3.substring(0, 5) + "." + b3.substring(5) + mod10(b3) + " "
                + "1 " + String.format("%04d", factor % 10000) + value;
    }

    private static String field(String id, String value) {
        return id + String.format("%02d", value.getBytes(StandardCharsets.UTF_8).length) + value;
    }

    private static int mod10(String digits) {
        int sum = 0;
        boolean two = true;
        for (int i = digits.length() - 1; i >= 0; i--) {
            int d = (digits.charAt(i) - '0') * (two ? 2 : 1);
            sum += d > 9 ? d - 9 : d;
            two = !two;
        }
        int dv = 10 - (sum % 10);
        return dv == 10 ? 0 : dv;
    }

    private static String crc16(String payload) {
        int crc = 0xFFFF;
        for (byte b : payload.getBytes(StandardCharsets.UTF_8)) {
            crc ^= (b & 0xFF) << 8;
            for (int i = 0; i < 8; i++) {
                crc = (crc & 0x8000) != 0 ? (crc << 1) ^ 0x1021 : crc << 1;
                crc &= 0xFFFF;
            }
        }
        return String.format("%04X", crc);
    }
}
