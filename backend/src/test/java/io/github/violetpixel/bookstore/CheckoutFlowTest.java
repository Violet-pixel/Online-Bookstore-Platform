package io.github.violetpixel.bookstore;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import com.jayway.jsonpath.JsonPath;

import io.github.violetpixel.bookstore.order.OrderRepository;
import io.github.violetpixel.bookstore.order.OrderService;

class CheckoutFlowTest extends IntegrationTest {

    @Autowired
    OrderRepository orders;

    @Autowired
    OrderService orderService;

    @Autowired
    PlatformTransactionManager txManager;

    private String order(String token, long addressId, String coupon) throws Exception {
        String r = mvc.perform(json(post("/api/orders"), token,
                "{\"addressId\":" + addressId + ",\"shippingMethod\":\"EXPRESSA\"" + (coupon == null ? "" : ",\"couponCode\":\"" + coupon + "\"") + "}"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.status").value("PENDING_PAYMENT"))
            .andExpect(jsonPath("$.canPay").value(true))
            .andReturn().getResponse().getContentAsString();
        return JsonPath.read(r, "$.code");
    }

    private void addToCart(String token, long bookId, int qty) throws Exception {
        mvc.perform(json(post("/api/cart/items"), token, "{\"bookId\":" + bookId + ",\"quantity\":" + qty + "}"))
            .andExpect(status().isOk());
    }

    private String cardToken(String token, String number) throws Exception {
        String r = mvc.perform(json(post("/api/payments/card-tokens"), token,
                "{\"number\":\"" + number + "\",\"holderName\":\"PESSOA TESTE\",\"expMonth\":12,\"expYear\":2031,\"cvv\":\"123\"}"))
            .andExpect(status().isCreated())
            .andReturn().getResponse().getContentAsString();
        assertThat(r).doesNotContain(number.replace(" ", "")).doesNotContain("123\"");
        return JsonPath.read(r, "$.token");
    }

    @Test
    void compraComCartaoDoCarrinhoAEntrega() throws Exception {
        String adminToken = admin();
        long bookA = bookId("a-metamorfose");
        long bookB = bookId("o-alienista");
        setStock(adminToken, bookA, 50);
        setStock(adminToken, bookB, 50);

        String token = newCustomer();
        long address = createAddress(token);
        addToCart(token, bookA, 2);
        addToCart(token, bookB, 1);

        mvc.perform(auth(get("/api/cart"), token)).andExpect(status().isOk())
            .andExpect(jsonPath("$.itemCount").value(3))
            .andExpect(jsonPath("$.subtotal").value(59.70));

        mvc.perform(json(post("/api/checkout/quote"), token, "{\"addressId\":" + address + ",\"couponCode\":\"bemvindo10\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.couponValid").value(true))
            .andExpect(jsonPath("$.discount").value(5.97))
            .andExpect(jsonPath("$.shippingOptions.length()").value(3));

        String code = order(token, address, "BEMVINDO10");
        assertThat(code).startsWith("PED-");
        assertThat(stock("a-metamorfose")).isEqualTo(48);
        mvc.perform(auth(get("/api/cart"), token)).andExpect(jsonPath("$.itemCount").value(0));

        mvc.perform(auth(get("/api/orders/" + code + "/installments"), token)).andExpect(status().isOk())
            .andExpect(jsonPath("$[0].count").value(1));

        String card = cardToken(token, "4111 1111 1111 1111");
        mvc.perform(json(post("/api/orders/" + code + "/payments"), token,
                "{\"method\":\"CREDIT_CARD\",\"cardToken\":\"" + card + "\",\"installments\":1}"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.status").value("APPROVED"))
            .andExpect(jsonPath("$.cardBrand").value("VISA"))
            .andExpect(jsonPath("$.cardLast4").value("1111"))
            .andExpect(jsonPath("$.orderStatus").value("PAID"));

        // o token do cartão é de uso único
        mvc.perform(json(post("/api/orders/" + code + "/payments"), token,
                "{\"method\":\"CREDIT_CARD\",\"cardToken\":\"" + card + "\",\"installments\":1}"))
            .andExpect(status().isConflict());

        mvc.perform(json(post("/api/admin/orders/" + code + "/shipment/advance"), adminToken, "{}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("SHIPPED"))
            .andExpect(jsonPath("$.shipment.trackingCode", startsWith("LV")));
        mvc.perform(json(post("/api/admin/orders/" + code + "/shipment/advance"), adminToken, "{\"status\":\"DELIVERED\"}"))
            .andExpect(jsonPath("$.status").value("DELIVERED"));

        mvc.perform(auth(get("/api/orders/" + code), token)).andExpect(status().isOk())
            .andExpect(jsonPath("$.shipment.status").value("DELIVERED"))
            .andExpect(jsonPath("$.shipment.events.length()", greaterThanOrEqualTo(5)))
            .andExpect(jsonPath("$.canCancel").value(false))
            .andExpect(jsonPath("$.payments[0].status").value("APPROVED"));

        mvc.perform(auth(get("/api/orders"), token)).andExpect(jsonPath("$.total").value(1))
            .andExpect(jsonPath("$.items[0].code").value(code));

        // outra pessoa não enxerga o pedido
        mvc.perform(auth(get("/api/orders/" + code), newCustomer())).andExpect(status().isNotFound());
    }

    @Test
    void cartaoRecusadoDepoisPixConfirmadoNoSandbox() throws Exception {
        long book = bookId("dom-casmurro");
        setStock(admin(), book, 40);
        String token = newCustomer();
        long address = createAddress(token);
        addToCart(token, book, 1);
        String code = order(token, address, null);

        String declined = cardToken(token, "4000 0000 0000 0002");
        mvc.perform(json(post("/api/orders/" + code + "/payments"), token,
                "{\"method\":\"CREDIT_CARD\",\"cardToken\":\"" + declined + "\"}"))
            .andExpect(jsonPath("$.status").value("DECLINED"))
            .andExpect(jsonPath("$.orderStatus").value("PENDING_PAYMENT"));

        String key = UUID.randomUUID().toString();
        String pix = mvc.perform(json(post("/api/orders/" + code + "/payments"), token, "{\"method\":\"PIX\"}")
                .header("Idempotency-Key", key))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.status").value("PENDING"))
            .andExpect(jsonPath("$.pixPayload", startsWith("000201")))
            .andReturn().getResponse().getContentAsString();
        String reference = JsonPath.read(pix, "$.reference");

        // mesma chave de idempotência devolve o mesmo pagamento
        mvc.perform(json(post("/api/orders/" + code + "/payments"), token, "{\"method\":\"PIX\"}").header("Idempotency-Key", key))
            .andExpect(jsonPath("$.reference").value(reference));

        mvc.perform(auth(post("/api/sandbox/payments/" + reference + "/confirm"), token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("APPROVED"))
            .andExpect(jsonPath("$.orderStatus").value("PAID"));
        mvc.perform(auth(get("/api/payments/" + reference), token)).andExpect(jsonPath("$.status").value("APPROVED"));
    }

    @Test
    void cancelarDevolveEstoqueEBoletoEstendePrazo() throws Exception {
        long book = bookId("frankenstein");
        setStock(admin(), book, 30);
        String token = newCustomer();
        long address = createAddress(token);
        addToCart(token, book, 3);
        String code = order(token, address, null);
        assertThat(stock("frankenstein")).isEqualTo(27);

        mvc.perform(json(post("/api/orders/" + code + "/payments"), token, "{\"method\":\"BOLETO\"}"))
            .andExpect(jsonPath("$.status").value("PENDING"))
            .andExpect(jsonPath("$.boletoLine").exists());
        String due = JsonPath.read(body(auth(get("/api/orders/" + code), token)), "$.paymentDueAt");
        assertThat(Instant.parse(due)).isAfter(Instant.now().plusSeconds(24 * 3600));

        mvc.perform(json(post("/api/orders/" + code + "/cancel"), token, "{\"reason\":\"Mudei de ideia\"}"))
            .andExpect(jsonPath("$.status").value("CANCELED"))
            .andExpect(jsonPath("$.payments[0].status").value("CANCELED"));
        assertThat(stock("frankenstein")).isEqualTo(30);
    }

    @Test
    void pedidoSemPagamentoExpiraEVoltaAoEstoque() throws Exception {
        long book = bookId("o-cortico");
        setStock(admin(), book, 20);
        String token = newCustomer();
        long address = createAddress(token);
        addToCart(token, book, 2);
        String code = order(token, address, null);

        new TransactionTemplate(txManager).executeWithoutResult(s ->
                orders.findByCode(code).orElseThrow().setPaymentDueAt(Instant.now().minusSeconds(60)));
        assertThat(orderService.expireOverdue(Instant.now())).isGreaterThanOrEqualTo(1);

        mvc.perform(auth(get("/api/orders/" + code), token))
            .andExpect(jsonPath("$.status").value("CANCELED"))
            .andExpect(jsonPath("$.cancelReason").value("Pagamento não confirmado dentro do prazo."));
        assertThat(stock("o-cortico")).isEqualTo(20);
    }

    @Test
    void naoVendeMaisDoQueOEstoque() throws Exception {
        long book = bookId("redis-em-producao");
        String adminToken = admin();
        setStock(adminToken, book, 2);
        String token = newCustomer();
        long address = createAddress(token);
        addToCart(token, book, 2);
        mvc.perform(json(post("/api/cart/items"), token, "{\"bookId\":" + book + ",\"quantity\":1}"))
            .andExpect(status().isUnprocessableContent());
        setStock(adminToken, book, 1);
        mvc.perform(json(post("/api/orders"), token, "{\"addressId\":" + address + ",\"shippingMethod\":\"ECONOMICA\"}"))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.code").value("estoque_insuficiente"));
        assertThat(stock("redis-em-producao")).isEqualTo(1);
        setStock(adminToken, book, 2);
    }

    @Test
    void pixEBoletoConvivemAteUmSerPago() throws Exception {
        long book = bookId("eu");
        setStock(admin(), book, 20);
        String token = newCustomer();
        long address = createAddress(token);
        addToCart(token, book, 1);
        String code = order(token, address, null);
        String pix = JsonPath.read(body(json(post("/api/orders/" + code + "/payments"), token, "{\"method\":\"PIX\"}")), "$.reference");
        String boleto = JsonPath.read(body(json(post("/api/orders/" + code + "/payments"), token, "{\"method\":\"BOLETO\"}")), "$.reference");
        mvc.perform(auth(get("/api/payments/" + pix), token)).andExpect(jsonPath("$.status").value("PENDING"));
        mvc.perform(auth(post("/api/sandbox/payments/" + boleto + "/confirm"), token)).andExpect(jsonPath("$.status").value("APPROVED"));
        mvc.perform(auth(get("/api/payments/" + pix), token)).andExpect(jsonPath("$.status").value("CANCELED"));
        mvc.perform(auth(post("/api/sandbox/payments/" + pix + "/confirm"), token)).andExpect(status().isConflict());
    }
}
