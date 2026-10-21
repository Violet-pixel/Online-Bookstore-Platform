package io.github.violetpixel.bookstore.cart;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.github.violetpixel.bookstore.cart.CartDtos.AddItem;
import io.github.violetpixel.bookstore.cart.CartDtos.CartDto;
import io.github.violetpixel.bookstore.cart.CartDtos.CartLine;
import io.github.violetpixel.bookstore.catalog.Book;
import io.github.violetpixel.bookstore.catalog.BookRepository;
import io.github.violetpixel.bookstore.catalog.CatalogDtos.BookCard;
import io.github.violetpixel.bookstore.common.ApiException;
import io.github.violetpixel.bookstore.common.Money;

@Service
public class CartService {

    public static final int MAX_PER_ITEM = 10;
    static final int MAX_LINES = 50;

    private final CartRepository carts;
    private final BookRepository books;

    public CartService(CartRepository carts, BookRepository books) {
        this.carts = carts;
        this.books = books;
    }

    @Transactional
    public CartDto view(long userId) {
        return toDto(cart(userId));
    }

    @Transactional
    public CartDto add(long userId, AddItem req) {
        Cart cart = cart(userId);
        addTo(cart, req.bookId(), req.quantity(), true);
        return toDto(cart);
    }

    @Transactional
    public CartDto change(long userId, long bookId, int quantity) {
        Cart cart = cart(userId);
        CartItem item = cart.item(bookId).orElseThrow(() -> ApiException.notFound("Item do carrinho"));
        if (quantity <= 0) {
            cart.getItems().remove(item);
        } else {
            Book book = item.getBook();
            if (quantity > book.getStockQuantity()) {
                throw ApiException.unprocessable("estoque_insuficiente",
                        "Só temos " + book.getStockQuantity() + " unidade(s) de “" + book.getTitle() + "”.");
            }
            item.setQuantity(quantity);
        }
        cart.touch();
        return toDto(cart);
    }

    @Transactional
    public CartDto remove(long userId, long bookId) {
        Cart cart = cart(userId);
        cart.getItems().removeIf(i -> i.getBook().getId() == bookId);
        cart.touch();
        return toDto(cart);
    }

    @Transactional
    public CartDto merge(long userId, List<AddItem> items) {
        Cart cart = cart(userId);
        if (items != null) {
            for (AddItem i : items) {
                addTo(cart, i.bookId(), i.quantity(), false);
            }
        }
        return toDto(cart);
    }

    @Transactional
    public void clear(long userId) {
        carts.findByUserId(userId).ifPresent(c -> {
            c.getItems().clear();
            c.touch();
        });
    }

    @Transactional
    public Cart cart(long userId) {
        return carts.findByUserId(userId).orElseGet(() -> carts.save(new Cart(userId)));
    }

    private void addTo(Cart cart, long bookId, int quantity, boolean strict) {
        Book book = books.findById(bookId).filter(Book::isActive).orElse(null);
        if (book == null) {
            if (strict) {
                throw ApiException.notFound("Livro");
            }
            return;
        }
        if (!book.inStock()) {
            if (strict) {
                throw ApiException.unprocessable("esgotado", "“" + book.getTitle() + "” está esgotado no momento.");
            }
            return;
        }
        CartItem item = cart.item(bookId).orElse(null);
        int wanted = (item == null ? 0 : item.getQuantity()) + quantity;
        int allowed = Math.min(Math.min(wanted, MAX_PER_ITEM), book.getStockQuantity());
        if (strict && allowed < wanted) {
            throw ApiException.unprocessable("estoque_insuficiente",
                    "Você pode levar até " + Math.min(MAX_PER_ITEM, book.getStockQuantity()) + " unidade(s) deste livro.");
        }
        if (item == null) {
            if (cart.getItems().size() >= MAX_LINES) {
                throw ApiException.unprocessable("carrinho_cheio", "O carrinho aceita até 50 títulos diferentes.");
            }
            cart.getItems().add(new CartItem(cart, book, allowed));
        } else {
            item.setQuantity(allowed);
        }
        cart.touch();
    }

    static CartDto toDto(Cart cart) {
        BigDecimal subtotal = Money.ZERO;
        BigDecimal savings = Money.ZERO;
        int count = 0;
        int weight = 0;
        List<CartLine> lines = new java.util.ArrayList<>();
        for (CartItem item : cart.getItems()) {
            Book b = item.getBook();
            BigDecimal line = Money.scale(b.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
            subtotal = subtotal.add(line);
            savings = savings.add(Money.max(Money.ZERO,
                    b.getListPrice().subtract(b.getPrice()).multiply(BigDecimal.valueOf(item.getQuantity()))));
            count += item.getQuantity();
            weight += b.getWeightGrams() * item.getQuantity();
            lines.add(new CartLine(BookCard.of(b), item.getQuantity(), b.getStockQuantity(), b.getPrice(), line));
        }
        return new CartDto(lines, count, Money.scale(subtotal), Money.scale(savings), weight);
    }
}
