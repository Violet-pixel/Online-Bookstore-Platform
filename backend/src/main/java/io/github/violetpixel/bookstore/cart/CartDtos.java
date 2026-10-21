package io.github.violetpixel.bookstore.cart;

import java.math.BigDecimal;
import java.util.List;

import io.github.violetpixel.bookstore.catalog.CatalogDtos.BookCard;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public final class CartDtos {

    private CartDtos() { }

    public record AddItem(@NotNull Long bookId, @Min(1) @Max(CartService.MAX_PER_ITEM) int quantity) {
    }

    public record ChangeQuantity(@Min(0) @Max(CartService.MAX_PER_ITEM) int quantity) {
    }

    public record MergeRequest(@Size(max = 50) List<@Valid AddItem> items) {
    }

    public record CartLine(BookCard book, int quantity, int available, BigDecimal unitPrice, BigDecimal lineTotal) {
    }

    public record CartDto(List<CartLine> items, int itemCount, BigDecimal subtotal, BigDecimal savings, int weightGrams) {
    }
}
