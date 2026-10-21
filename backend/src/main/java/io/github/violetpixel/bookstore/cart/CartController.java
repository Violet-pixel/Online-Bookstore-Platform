package io.github.violetpixel.bookstore.cart;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import io.github.violetpixel.bookstore.auth.CurrentUser;
import io.github.violetpixel.bookstore.cart.CartDtos.AddItem;
import io.github.violetpixel.bookstore.cart.CartDtos.CartDto;
import io.github.violetpixel.bookstore.cart.CartDtos.ChangeQuantity;
import io.github.violetpixel.bookstore.cart.CartDtos.MergeRequest;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/cart")
@Tag(name = "Carrinho")
public class CartController {

    private final CartService carts;

    public CartController(CartService carts) {
        this.carts = carts;
    }

    @GetMapping
    public CartDto view(Authentication auth) {
        return carts.view(CurrentUser.id(auth));
    }

    @PostMapping("/items")
    public CartDto add(Authentication auth, @Valid @RequestBody AddItem req) {
        return carts.add(CurrentUser.id(auth), req);
    }

    @PatchMapping("/items/{bookId}")
    public CartDto change(Authentication auth, @PathVariable long bookId, @Valid @RequestBody ChangeQuantity req) {
        return carts.change(CurrentUser.id(auth), bookId, req.quantity());
    }

    @DeleteMapping("/items/{bookId}")
    public CartDto remove(Authentication auth, @PathVariable long bookId) {
        return carts.remove(CurrentUser.id(auth), bookId);
    }

    /** Junta o carrinho guardado no navegador (antes do login) com o da conta. */
    @PostMapping("/merge")
    public CartDto merge(Authentication auth, @Valid @RequestBody MergeRequest req) {
        return carts.merge(CurrentUser.id(auth), req.items());
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void clear(Authentication auth) {
        carts.clear(CurrentUser.id(auth));
    }
}
