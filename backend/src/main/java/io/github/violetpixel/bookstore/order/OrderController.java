package io.github.violetpixel.bookstore.order;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import io.github.violetpixel.bookstore.auth.CurrentUser;
import io.github.violetpixel.bookstore.common.PageDto;
import io.github.violetpixel.bookstore.order.OrderDtos.CancelRequest;
import io.github.violetpixel.bookstore.order.OrderDtos.CheckoutRequest;
import io.github.violetpixel.bookstore.order.OrderDtos.OrderDetailDto;
import io.github.violetpixel.bookstore.order.OrderDtos.OrderSummaryDto;
import io.github.violetpixel.bookstore.order.OrderDtos.QuoteDto;
import io.github.violetpixel.bookstore.order.OrderDtos.QuoteRequest;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api")
@Tag(name = "Pedidos")
public class OrderController {

    private final CheckoutService checkout;
    private final OrderService orders;

    public OrderController(CheckoutService checkout, OrderService orders) {
        this.checkout = checkout;
        this.orders = orders;
    }

    @PostMapping("/checkout/quote")
    public QuoteDto quote(Authentication auth, @Valid @RequestBody QuoteRequest req) {
        return checkout.quote(CurrentUser.id(auth), req);
    }

    @PostMapping("/orders")
    @ResponseStatus(HttpStatus.CREATED)
    public OrderDetailDto place(Authentication auth, @Valid @RequestBody CheckoutRequest req) {
        return checkout.checkout(CurrentUser.id(auth), req);
    }

    @GetMapping("/orders")
    public PageDto<OrderSummaryDto> mine(Authentication auth, @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
        return orders.mine(CurrentUser.id(auth), status, page, size);
    }

    @GetMapping("/orders/{code}")
    public OrderDetailDto detail(Authentication auth, @PathVariable String code) {
        return orders.detail(CurrentUser.id(auth), code);
    }

    @PostMapping("/orders/{code}/cancel")
    public OrderDetailDto cancel(Authentication auth, @PathVariable String code, @Valid @RequestBody(required = false) CancelRequest req) {
        return orders.cancel(CurrentUser.id(auth), code, req == null ? null : req.reason());
    }
}
