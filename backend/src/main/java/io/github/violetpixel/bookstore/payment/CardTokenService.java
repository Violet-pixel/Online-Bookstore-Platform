package io.github.violetpixel.bookstore.payment;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.HexFormat;
import java.util.Map;
import java.util.Optional;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import io.github.violetpixel.bookstore.common.ApiException;
import io.github.violetpixel.bookstore.common.Text;
import io.github.violetpixel.bookstore.payment.PaymentDtos.CardRequest;
import io.github.violetpixel.bookstore.payment.PaymentDtos.CardToken;

/**
 * Tokenização no estilo dos gateways: o número do cartão é validado (Luhn, validade, bandeira) e descartado.
 * No Redis fica só um token de 10 minutos com bandeira, 4 últimos dígitos e o resultado previsto pelo sandbox.
 */
@Service
public class CardTokenService {

    static final Duration TTL = Duration.ofMinutes(10);
    private static final String PREFIX = "livraria:pay:card:";

    public record TokenData(long userId, String brand, String last4, String outcome) {
    }

    private final StringRedisTemplate redis;
    private final SecureRandom random = new SecureRandom();

    public CardTokenService(StringRedisTemplate redis) {
        this.redis = redis;
    }

    public CardToken tokenize(long userId, CardRequest card) {
        String number = Text.digits(card.number());
        if (number.length() < 13 || number.length() > 19 || !luhn(number)) {
            throw ApiException.badRequest("cartao_invalido", "Número de cartão inválido.");
        }
        YearMonth expiry = YearMonth.of(card.expYear(), card.expMonth());
        if (expiry.isBefore(YearMonth.now(ZoneId.of("America/Sao_Paulo")))) {
            throw ApiException.badRequest("cartao_vencido", "Este cartão está vencido.");
        }
        String brand = brand(number);
        int cvvLength = "AMEX".equals(brand) ? 4 : 3;
        if (card.cvv().length() != cvvLength) {
            throw ApiException.badRequest("cvv_invalido", "Código de segurança inválido.");
        }
        byte[] bytes = new byte[18];
        random.nextBytes(bytes);
        String token = "tok_" + HexFormat.of().formatHex(bytes);
        String last4 = number.substring(number.length() - 4);
        redis.opsForHash().putAll(PREFIX + token, Map.of(
                "userId", String.valueOf(userId),
                "brand", brand,
                "last4", last4,
                "outcome", SandboxGateway.outcomeFor(number)));
        redis.expire(PREFIX + token, TTL);
        return new CardToken(token, brand, last4, Instant.now().plus(TTL));
    }

    /** Consome o token (uso único). */
    public Optional<TokenData> consume(String token, long userId) {
        if (token == null || !token.startsWith("tok_")) {
            return Optional.empty();
        }
        String key = PREFIX + token;
        Map<Object, Object> data = redis.opsForHash().entries(key);
        redis.delete(key);
        if (data.isEmpty() || !String.valueOf(userId).equals(data.get("userId"))) {
            return Optional.empty();
        }
        return Optional.of(new TokenData(userId, (String) data.get("brand"), (String) data.get("last4"), (String) data.get("outcome")));
    }

    static boolean luhn(String digits) {
        int sum = 0;
        boolean dbl = false;
        for (int i = digits.length() - 1; i >= 0; i--) {
            int d = digits.charAt(i) - '0';
            if (dbl) {
                d *= 2;
                if (d > 9) {
                    d -= 9;
                }
            }
            sum += d;
            dbl = !dbl;
        }
        return sum % 10 == 0;
    }

    static String brand(String n) {
        if (n.matches("^(4011|4312|4389|4514|4576|5041|5066|5067|509|6277|6362|6363|650|6516|6550).*")) {
            return "ELO";
        }
        if (n.matches("^(606282|3841).*")) {
            return "HIPERCARD";
        }
        if (n.startsWith("4")) {
            return "VISA";
        }
        if (n.matches("^(5[1-5]|2(2[2-9]|[3-6]\\d|7[01]|720)).*")) {
            return "MASTERCARD";
        }
        if (n.matches("^3[47].*")) {
            return "AMEX";
        }
        return "OUTRA";
    }
}
