package io.github.violetpixel.bookstore;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import jakarta.servlet.http.Cookie;

class AuthTest extends IntegrationTest {

    @Test
    void cadastroLoginRenovacaoESaida() throws Exception {
        String email = "auth-" + UUID.randomUUID().toString().substring(0, 8) + "@prelo.dev";
        MvcResult reg = mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                .content("{\"fullName\":\"Ana Teste\",\"email\":\"" + email.toUpperCase() + "\",\"password\":\"senha-forte-1\"}"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.user.email").value(email))
            .andExpect(jsonPath("$.user.role").value("CUSTOMER"))
            .andExpect(cookie().httpOnly("livraria_refresh", true))
            .andReturn();
        String token = com.jayway.jsonpath.JsonPath.read(reg.getResponse().getContentAsString(), "$.accessToken");
        Cookie refresh = reg.getResponse().getCookie("livraria_refresh");
        assertThat(refresh).isNotNull();

        mvc.perform(auth(get("/api/auth/me"), token)).andExpect(status().isOk()).andExpect(jsonPath("$.fullName").value("Ana Teste"));
        mvc.perform(get("/api/auth/me")).andExpect(status().isUnauthorized()).andExpect(jsonPath("$.code").value("nao_autenticado"));

        // e-mail repetido
        mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                .content("{\"fullName\":\"Outra\",\"email\":\"" + email + "\",\"password\":\"senha-forte-1\"}"))
            .andExpect(status().isConflict());

        // renovação gira o token: o antigo não vale mais
        MvcResult renewed = mvc.perform(post("/api/auth/refresh").cookie(refresh)).andExpect(status().isOk()).andReturn();
        mvc.perform(post("/api/auth/refresh").cookie(refresh)).andExpect(status().isUnauthorized());
        Cookie second = renewed.getResponse().getCookie("livraria_refresh");
        mvc.perform(post("/api/auth/logout").cookie(second)).andExpect(status().isNoContent());
        mvc.perform(post("/api/auth/refresh").cookie(second)).andExpect(status().isUnauthorized());
    }

    @Test
    void bloqueiaDepoisDeCincoSenhasErradas() throws Exception {
        String email = "lock-" + UUID.randomUUID().toString().substring(0, 8) + "@prelo.dev";
        mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                .content("{\"fullName\":\"Bia\",\"email\":\"" + email + "\",\"password\":\"senha-forte-1\"}"))
            .andExpect(status().isCreated());
        for (int i = 0; i < 5; i++) {
            mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                    .content("{\"email\":\"" + email + "\",\"password\":\"errada\"}"))
                .andExpect(status().isUnauthorized());
        }
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"" + email + "\",\"password\":\"senha-forte-1\"}"))
            .andExpect(status().isTooManyRequests())
            .andExpect(jsonPath("$.code").value("muitas_tentativas"));
    }

    @Test
    void validaCampos() throws Exception {
        mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                .content("{\"fullName\":\"\",\"email\":\"nao-e-email\",\"password\":\"123\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("dados_invalidos"))
            .andExpect(jsonPath("$.fields.email").exists())
            .andExpect(jsonPath("$.fields.password").exists());
    }

    @Test
    void clienteNaoEntraNaAdministracao() throws Exception {
        String token = newCustomer();
        mvc.perform(auth(get("/api/admin/dashboard"), token)).andExpect(status().isForbidden());
        mvc.perform(auth(get("/api/admin/dashboard"), admin())).andExpect(status().isOk())
            .andExpect(jsonPath("$.ordersByStatus.PAID").exists());
    }
}
