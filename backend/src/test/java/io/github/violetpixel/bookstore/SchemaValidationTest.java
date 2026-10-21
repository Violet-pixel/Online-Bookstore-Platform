package io.github.violetpixel.bookstore;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import org.springframework.test.context.TestPropertySource;

/**
 * Sobe a aplicação com {@code ddl-auto=validate}: o Hibernate confere cada entidade contra as tabelas criadas
 * pelo Flyway. Roda no PostgreSQL e no MySQL, onde os tipos reportados pelo driver batem um a um com o mapeamento.
 */
@TestPropertySource(properties = "spring.jpa.hibernate.ddl-auto=validate")
@EnabledIf("strictVendor")
class SchemaValidationTest extends IntegrationTest {

    static boolean strictVendor() {
        String vendor = System.getenv().getOrDefault("DB_VENDOR", "postgresql");
        return vendor.equals("postgresql") || vendor.equals("mysql");
    }

    @Test
    void entidadesBatemComAsMigracoes() throws Exception {
        mvc.perform(get("/actuator/health")).andExpect(status().isOk());
    }
}
