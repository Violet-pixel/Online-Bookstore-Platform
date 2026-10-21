package io.github.violetpixel.bookstore.shipping;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.github.violetpixel.bookstore.common.ApiException;
import io.github.violetpixel.bookstore.common.Money;
import io.github.violetpixel.bookstore.common.Text;

/**
 * Cotação de frete por região do CEP (primeiro dígito) e peso. As tabelas são de demonstração e ficam no banco
 * ({@code shipping_method}); a região só ajusta o valor e o prazo.
 */
@Service
public class ShippingQuoteService {

    public record ShippingOption(String code, String name, String carrier, String description, BigDecimal fee,
            int minDays, int maxDays, boolean free) {
    }

    private record Region(String name, BigDecimal factor, int extraDays) {
    }

    private final ShippingMethodRepository methods;

    public ShippingQuoteService(ShippingMethodRepository methods) {
        this.methods = methods;
    }

    @Transactional(readOnly = true)
    public List<ShippingOption> quote(String zipCode, int weightGrams, BigDecimal subtotal) {
        String zip = Text.digits(zipCode);
        if (zip.length() != 8) {
            throw ApiException.badRequest("cep_invalido", "Informe um CEP com 8 dígitos.");
        }
        Region region = region(zip.charAt(0));
        BigDecimal kg = BigDecimal.valueOf(Math.max(weightGrams, 1)).divide(BigDecimal.valueOf(1000), 0, RoundingMode.CEILING);
        return methods.findAllByOrderBySortOrderAsc().stream()
            .filter(ShippingMethod::isActive)
            .map(m -> {
                boolean free = m.getFreeAbove() != null && subtotal.compareTo(m.getFreeAbove()) >= 0;
                BigDecimal fee = free ? Money.ZERO
                        : Money.scale(m.getBaseFee().add(m.getFeePerKg().multiply(kg)).multiply(region.factor()));
                return new ShippingOption(m.getCode(), m.getName(), m.getCarrier(), m.getDescription(), fee,
                        m.getMinDays() + region.extraDays(), m.getMaxDays() + region.extraDays(), free);
            })
            .toList();
    }

    public ShippingOption option(String zipCode, int weightGrams, BigDecimal subtotal, String code) {
        return quote(zipCode, weightGrams, subtotal).stream().filter(o -> o.code().equals(code)).findFirst()
            .orElseThrow(() -> ApiException.badRequest("frete_invalido", "Escolha uma forma de envio válida."));
    }

    private static Region region(char first) {
        return switch (first) {
            case '0', '1' -> new Region("SP", new BigDecimal("1.00"), 0);
            case '2' -> new Region("RJ/ES", new BigDecimal("1.05"), 0);
            case '3' -> new Region("MG", new BigDecimal("1.10"), 1);
            case '4' -> new Region("BA/SE", new BigDecimal("1.30"), 2);
            case '5' -> new Region("PE/AL/PB/RN", new BigDecimal("1.40"), 3);
            case '6' -> new Region("Norte e CE/PI/MA", new BigDecimal("1.55"), 4);
            case '7' -> new Region("Centro-Oeste", new BigDecimal("1.30"), 2);
            case '8' -> new Region("PR/SC", new BigDecimal("1.15"), 1);
            default -> new Region("RS", new BigDecimal("1.20"), 2);
        };
    }
}
