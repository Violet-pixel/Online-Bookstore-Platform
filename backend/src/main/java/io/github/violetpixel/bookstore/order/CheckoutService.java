package io.github.violetpixel.bookstore.order;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.github.violetpixel.bookstore.account.AccountService;
import io.github.violetpixel.bookstore.account.Address;
import io.github.violetpixel.bookstore.cart.Cart;
import io.github.violetpixel.bookstore.cart.CartItem;
import io.github.violetpixel.bookstore.cart.CartService;
import io.github.violetpixel.bookstore.catalog.Author;
import io.github.violetpixel.bookstore.catalog.Book;
import io.github.violetpixel.bookstore.catalog.BookRepository;
import io.github.violetpixel.bookstore.catalog.CatalogCache;
import io.github.violetpixel.bookstore.common.ApiException;
import io.github.violetpixel.bookstore.common.Money;
import io.github.violetpixel.bookstore.config.AppProperties;
import io.github.violetpixel.bookstore.order.CouponService.CouponResult;
import io.github.violetpixel.bookstore.order.OrderDtos.CheckoutRequest;
import io.github.violetpixel.bookstore.order.OrderDtos.OrderDetailDto;
import io.github.violetpixel.bookstore.order.OrderDtos.QuoteDto;
import io.github.violetpixel.bookstore.order.OrderDtos.QuoteRequest;
import io.github.violetpixel.bookstore.shipping.ShippingQuoteService;
import io.github.violetpixel.bookstore.shipping.ShippingQuoteService.ShippingOption;

/** Fecha a compra: confere o carrinho, reserva o estoque, aplica cupom e frete e cria o pedido aguardando pagamento. */
@Service
public class CheckoutService {

    private static final String CODE_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final DateTimeFormatter CODE_DATE = DateTimeFormatter.ofPattern("yyMMdd").withZone(ZoneId.of("America/Sao_Paulo"));

    private final CartService carts;
    private final AccountService account;
    private final BookRepository books;
    private final OrderRepository orders;
    private final CouponService coupons;
    private final ShippingQuoteService shipping;
    private final OrderMapper mapper;
    private final CatalogCache cache;
    private final AppProperties props;
    private final SecureRandom random = new SecureRandom();

    public CheckoutService(CartService carts, AccountService account, BookRepository books, OrderRepository orders,
            CouponService coupons, ShippingQuoteService shipping, OrderMapper mapper, CatalogCache cache, AppProperties props) {
        this.carts = carts;
        this.account = account;
        this.books = books;
        this.orders = orders;
        this.coupons = coupons;
        this.shipping = shipping;
        this.mapper = mapper;
        this.cache = cache;
        this.props = props;
    }

    @Transactional
    public QuoteDto quote(long userId, QuoteRequest req) {
        Cart cart = carts.cart(userId);
        Totals t = totals(cart);
        CouponResult coupon = coupons.evaluate(req.couponCode(), t.subtotal());
        String zip = req.zipCode();
        if (req.addressId() != null) {
            zip = account.address(userId, req.addressId()).getZipCode();
        }
        List<ShippingOption> options = zip == null || zip.isBlank() || t.count() == 0 ? List.of()
                : shipping.quote(zip, t.weight(), t.subtotal().subtract(coupon.discount()));
        return new QuoteDto(t.count(), t.weight(), t.subtotal(), coupon.discount(), coupon.code(), coupon.valid(),
                coupon.message(), options);
    }

    @Transactional
    public OrderDetailDto checkout(long userId, CheckoutRequest req) {
        Cart cart = carts.cart(userId);
        if (cart.getItems().isEmpty()) {
            throw ApiException.unprocessable("carrinho_vazio", "Seu carrinho está vazio.");
        }
        Address address = account.address(userId, req.addressId());

        List<CartItem> items = new ArrayList<>(cart.getItems());
        items.sort(Comparator.comparing(i -> i.getBook().getId()));
        for (CartItem item : items) {
            Book book = item.getBook();
            if (!book.isActive()) {
                throw ApiException.unprocessable("indisponivel", "“" + book.getTitle() + "” não está mais à venda.");
            }
            if (books.reserve(book.getId(), item.getQuantity()) == 0) {
                throw ApiException.conflict("estoque_insuficiente",
                        "Não há estoque suficiente de “" + book.getTitle() + "”. Ajuste a quantidade no carrinho.");
            }
        }

        Totals t = totals(cart);
        CouponResult coupon = coupons.evaluate(req.couponCode(), t.subtotal());
        if (req.couponCode() != null && !req.couponCode().isBlank() && !coupon.valid()) {
            throw ApiException.unprocessable("cupom_invalido", coupon.message());
        }
        BigDecimal afterDiscount = t.subtotal().subtract(coupon.discount());
        ShippingOption option = shipping.option(address.getZipCode(), t.weight(), afterDiscount, req.shippingMethod());

        Instant now = Instant.now();
        PurchaseOrder order = new PurchaseOrder(newCode(now), userId, ShippingAddress.of(address), now,
                now.plus(props.payments().orderHold()));
        order.setSubtotal(t.subtotal());
        order.setDiscount(coupon.discount());
        order.setCouponCode(coupon.valid() ? coupon.code() : null);
        order.setShippingFee(option.fee());
        order.setTotal(Money.scale(afterDiscount.add(option.fee())));
        order.setItemCount(t.count());
        order.setShipping(option.code(), option.name(), option.carrier(), option.minDays(), option.maxDays());
        for (CartItem item : items) {
            Book b = item.getBook();
            String authors = String.join(", ", b.getAuthors().stream().map(Author::getName).toList());
            order.getItems().add(new OrderItem(order, b.getId(), b.getSlug(), b.getTitle(),
                    authors.length() > 300 ? authors.substring(0, 300) : authors, b.getIsbn(), b.getCoverStyle(), b.getPrice(),
                    item.getQuantity(), Money.scale(b.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))));
        }
        orders.save(order);
        cache.evictBooks(items.stream().map(i -> i.getBook().getSlug()).toList());
        cart.getItems().clear();
        cart.touch();
        return mapper.detail(order, false);
    }

    private record Totals(int count, int weight, BigDecimal subtotal) {
    }

    private static Totals totals(Cart cart) {
        int count = 0;
        int weight = 0;
        BigDecimal subtotal = Money.ZERO;
        for (CartItem item : cart.getItems()) {
            count += item.getQuantity();
            weight += item.getBook().getWeightGrams() * item.getQuantity();
            subtotal = subtotal.add(item.getBook().getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
        }
        return new Totals(count, weight, Money.scale(subtotal));
    }

    String newCode(Instant now) {
        for (int attempt = 0; attempt < 10; attempt++) {
            StringBuilder sb = new StringBuilder("PED-").append(CODE_DATE.format(now)).append('-');
            for (int i = 0; i < 5; i++) {
                sb.append(CODE_CHARS.charAt(random.nextInt(CODE_CHARS.length())));
            }
            String code = sb.toString();
            if (!orders.existsByCode(code)) {
                return code;
            }
        }
        throw new IllegalStateException("Não foi possível gerar um código de pedido único.");
    }
}
