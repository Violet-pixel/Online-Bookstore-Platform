package io.github.violetpixel.bookstore.shipping;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.github.violetpixel.bookstore.shipping.ShippingQuoteService.ShippingOption;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@RestController
@RequestMapping("/api/shipping")
@Tag(name = "Envio")
public class ShippingController {

    public record QuoteRequest(@NotBlank String zipCode, @Min(1) @Max(100_000) int weightGrams, @NotNull BigDecimal subtotal) {
    }

    private final ShippingQuoteService quotes;

    public ShippingController(ShippingQuoteService quotes) {
        this.quotes = quotes;
    }

    /** Estimativa pública de frete (usada no carrinho antes do login). */
    @PostMapping("/quote")
    public List<ShippingOption> quote(@Valid @RequestBody QuoteRequest req) {
        return quotes.quote(req.zipCode(), req.weightGrams(), req.subtotal());
    }
}
