package io.github.violetpixel.bookstore.config;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/** Configurações próprias da aplicação (prefixo {@code app}). */
@ConfigurationProperties(prefix = "app")
public record AppProperties(
        @DefaultValue("Prelo") String name,
        @DefaultValue Jwt jwt,
        @DefaultValue Demo demo,
        @DefaultValue Payments payments,
        @DefaultValue("http://localhost:5173") List<String> corsOrigins,
        @DefaultValue("false") boolean https) {

    public record Jwt(
            @DefaultValue("") String secret,
            @DefaultValue("15m") Duration accessTtl,
            @DefaultValue("7d") Duration refreshTtl) {
    }

    public record Demo(
            @DefaultValue("true") boolean enabled,
            @DefaultValue("prelo.dev") String domain,
            @DefaultValue("Prelo@2026") String password) {
    }

    public record Payments(
            @DefaultValue("true") boolean sandbox,
            @DefaultValue("60m") Duration orderHold,
            @DefaultValue("30m") Duration pixTtl,
            @DefaultValue("3") int boletoDays,
            @DefaultValue("6") int maxInstallments,
            @DefaultValue("30.00") BigDecimal minInstallmentValue) {
    }
}
