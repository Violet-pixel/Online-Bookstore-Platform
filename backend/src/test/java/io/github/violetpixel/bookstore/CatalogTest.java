package io.github.violetpixel.bookstore;

import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

class CatalogTest extends IntegrationTest {

    @Test
    void vitrineComCatalogosECategorias() throws Exception {
        mvc.perform(get("/api/home")).andExpect(status().isOk())
            .andExpect(jsonPath("$.catalogs.length()", greaterThan(3)))
            .andExpect(jsonPath("$.catalogs[0].slug").value("mais-vendidos"))
            .andExpect(jsonPath("$.categories.length()").value(8))
            .andExpect(jsonPath("$.spotlight.length()").value(3));
        // segunda chamada vem do cache no Redis e precisa ser igual
        mvc.perform(get("/api/home")).andExpect(status().isOk()).andExpect(jsonPath("$.catalogs[0].slug").value("mais-vendidos"));
    }

    @Test
    void buscaIgnoraAcentosEMaiusculas() throws Exception {
        mvc.perform(get("/api/books").param("q", "MEMORIAS postumas"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.items[0].slug").value("memorias-postumas-de-bras-cubas"));
        mvc.perform(get("/api/books").param("q", "dostoievski"))
            .andExpect(jsonPath("$.items[*].title", hasItem("Crime e Castigo")));
        String isbn = com.jayway.jsonpath.JsonPath.read(body(get("/api/books/dom-casmurro")), "$.isbn");
        mvc.perform(get("/api/books").param("q", isbn.replace("-", "")))
            .andExpect(jsonPath("$.total").value(1))
            .andExpect(jsonPath("$.items[0].slug").value("dom-casmurro"));
    }

    @Test
    void filtrosOrdenacaoEPaginacao() throws Exception {
        mvc.perform(get("/api/books").param("category", "tecnologia").param("sort", "price_asc").param("size", "3"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.items.length()").value(3))
            .andExpect(jsonPath("$.items[*].category.slug", everyItem(is("tecnologia"))))
            .andExpect(jsonPath("$.totalPages").value(2));
        mvc.perform(get("/api/books").param("catalog", "para-programar-melhor"))
            .andExpect(jsonPath("$.total").value(6));
        mvc.perform(get("/api/books").param("format", "POCKET").param("maxPrice", "20"))
            .andExpect(jsonPath("$.items[*].format", everyItem(is("POCKET"))));
    }

    @Test
    void detalheDoLivroECatalogo() throws Exception {
        mvc.perform(get("/api/books/dom-casmurro")).andExpect(status().isOk())
            .andExpect(jsonPath("$.authors[0].name").value("Machado de Assis"))
            .andExpect(jsonPath("$.category.slug").value("literatura-brasileira"))
            .andExpect(jsonPath("$.related.length()", greaterThan(0)));
        mvc.perform(get("/api/books/nao-existe")).andExpect(status().isNotFound()).andExpect(jsonPath("$.code").value("nao_encontrado"));
        mvc.perform(get("/api/catalogs/lancamentos")).andExpect(status().isOk()).andExpect(jsonPath("$.bookCount").value(10));
        mvc.perform(get("/api/categories/poesia")).andExpect(status().isOk()).andExpect(jsonPath("$.bookCount").value(4));
    }

    @Test
    void freteFicaMaisCaroLongeEGratisAcimaDoLimite() throws Exception {
        mvc.perform(post("/api/shipping/quote").contentType(MediaType.APPLICATION_JSON)
                .content("{\"zipCode\":\"20040-000\",\"weightGrams\":800,\"subtotal\":50}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].code").value("ECONOMICA"))
            .andExpect(jsonPath("$[0].fee").value(20.37));
        mvc.perform(post("/api/shipping/quote").contentType(MediaType.APPLICATION_JSON)
                .content("{\"zipCode\":\"69000-000\",\"weightGrams\":800,\"subtotal\":250}"))
            .andExpect(jsonPath("$[0].free").value(true))
            .andExpect(jsonPath("$[1].fee").value(50.84));
        mvc.perform(post("/api/shipping/quote").contentType(MediaType.APPLICATION_JSON)
                .content("{\"zipCode\":\"123\",\"weightGrams\":800,\"subtotal\":50}"))
            .andExpect(status().isBadRequest());
    }
}
