package io.github.violetpixel.bookstore;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import com.jayway.jsonpath.JsonPath;

/**
 * Base dos testes: sobe a aplicação inteira contra o banco escolhido por DB_VENDOR (padrão PostgreSQL)
 * e um Redis de verdade (banco 15, separado do da aplicação). Os dados de demonstração são criados na primeira subida.
 */
@SpringBootTest(properties = {
        "app.jwt.secret=testes-testes-testes-testes-testes-0123456789",
        "app.jobs.enabled=false",
        "app.payments.sandbox=true",
        "app.demo.enabled=true",
        "spring.data.redis.database=15"})
@AutoConfigureMockMvc
public abstract class IntegrationTest {

    protected static final String ADMIN = "admin@prelo.dev";
    protected static final String CUSTOMER = "leitora@prelo.dev";
    protected static final String PASSWORD = "Prelo@2026";

    @Autowired
    protected MockMvc mvc;

    protected String login(String email, String password) throws Exception {
        MvcResult r = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}"))
            .andExpect(status().isOk()).andReturn();
        return JsonPath.read(r.getResponse().getContentAsString(), "$.accessToken");
    }

    protected String admin() throws Exception {
        return login(ADMIN, PASSWORD);
    }

    /** Cria uma conta nova e devolve o token (cada teste usa a sua, sem interferir nos outros). */
    protected String newCustomer() throws Exception {
        String email = "teste-" + UUID.randomUUID().toString().substring(0, 12) + "@prelo.dev";
        MvcResult r = mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                .content("{\"fullName\":\"Pessoa de Teste\",\"email\":\"" + email + "\",\"password\":\"senha-forte-1\"}"))
            .andExpect(status().isCreated()).andReturn();
        return JsonPath.read(r.getResponse().getContentAsString(), "$.accessToken");
    }

    protected static MockHttpServletRequestBuilder auth(MockHttpServletRequestBuilder req, String token) {
        return req.header(HttpHeaders.AUTHORIZATION, "Bearer " + token);
    }

    protected static MockHttpServletRequestBuilder json(MockHttpServletRequestBuilder req, String token, String body) {
        return auth(req, token).contentType(MediaType.APPLICATION_JSON).content(body);
    }

    protected String body(MockHttpServletRequestBuilder req) throws Exception {
        return mvc.perform(req).andReturn().getResponse().getContentAsString();
    }

    protected long bookId(String slug) throws Exception {
        Number id = JsonPath.read(body(get("/api/books/" + slug)), "$.id");
        return id.longValue();
    }

    protected int stock(String slug) throws Exception {
        return JsonPath.read(body(get("/api/books/" + slug)), "$.stockQuantity");
    }

    protected void setStock(String adminToken, long id, int quantity) throws Exception {
        mvc.perform(json(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch("/api/admin/books/" + id + "/stock"),
                adminToken, "{\"stockQuantity\":" + quantity + "}")).andExpect(status().isOk());
    }

    protected long createAddress(String token) throws Exception {
        String r = body(json(post("/api/account/addresses"), token, """
            {"label":"Casa","recipient":"Pessoa de Teste","zipCode":"20040-000","street":"Rua dos Testes","streetNumber":"10",
             "complement":null,"district":"Centro","city":"Rio de Janeiro","state":"rj","isDefault":true}"""));
        Number id = JsonPath.read(r, "$.id");
        return id.longValue();
    }
}
