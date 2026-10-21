package io.github.violetpixel.bookstore.admin;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import io.github.violetpixel.bookstore.admin.AdminDtos.AdminBookDto;
import io.github.violetpixel.bookstore.admin.AdminDtos.AdminCatalogDto;
import io.github.violetpixel.bookstore.admin.AdminDtos.AdminCategoryDto;
import io.github.violetpixel.bookstore.admin.AdminDtos.BookRequest;
import io.github.violetpixel.bookstore.admin.AdminDtos.CatalogRequest;
import io.github.violetpixel.bookstore.admin.AdminDtos.CategoryRequest;
import io.github.violetpixel.bookstore.admin.AdminDtos.DashboardDto;
import io.github.violetpixel.bookstore.admin.AdminDtos.LowStockDto;
import io.github.violetpixel.bookstore.admin.AdminDtos.StockRequest;
import io.github.violetpixel.bookstore.admin.AdminDtos.TopSellerDto;
import io.github.violetpixel.bookstore.auth.UserRepository;
import io.github.violetpixel.bookstore.catalog.BookRepository;
import io.github.violetpixel.bookstore.common.Money;
import io.github.violetpixel.bookstore.common.PageDto;
import io.github.violetpixel.bookstore.order.OrderDtos.AdvanceRequest;
import io.github.violetpixel.bookstore.order.OrderDtos.CancelRequest;
import io.github.violetpixel.bookstore.order.OrderDtos.OrderDetailDto;
import io.github.violetpixel.bookstore.order.OrderDtos.OrderSummaryDto;
import io.github.violetpixel.bookstore.order.OrderRepository;
import io.github.violetpixel.bookstore.order.OrderService;
import io.github.violetpixel.bookstore.order.OrderStatus;
import io.github.violetpixel.bookstore.shipping.ShipmentRepository;
import io.github.violetpixel.bookstore.shipping.ShipmentStatus;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/admin")
@Tag(name = "Administração")
public class AdminController {

    private final AdminCatalogService catalog;
    private final OrderService orders;
    private final OrderRepository orderRepo;
    private final ShipmentRepository shipments;
    private final BookRepository books;
    private final UserRepository users;

    public AdminController(AdminCatalogService catalog, OrderService orders, OrderRepository orderRepo,
            ShipmentRepository shipments, BookRepository books, UserRepository users) {
        this.catalog = catalog;
        this.orders = orders;
        this.orderRepo = orderRepo;
        this.shipments = shipments;
        this.books = books;
        this.users = users;
    }

    @GetMapping("/dashboard")
    @Transactional(readOnly = true)
    public DashboardDto dashboard() {
        Instant now = Instant.now();
        Instant startOfDay = LocalDate.now(ZoneId.of("America/Sao_Paulo")).atStartOfDay(ZoneId.of("America/Sao_Paulo")).toInstant();
        Instant last30 = now.minus(30, ChronoUnit.DAYS);
        BigDecimal revenue = Money.scale(orderRepo.revenueSince(last30));
        long paid = orderRepo.paidCountSince(last30);
        Map<String, Long> byStatus = new LinkedHashMap<>();
        for (OrderStatus s : OrderStatus.values()) {
            byStatus.put(s.name(), 0L);
        }
        for (Object[] row : orderRepo.countByStatusGrouped()) {
            byStatus.put(((OrderStatus) row[0]).name(), ((Number) row[1]).longValue());
        }
        List<LowStockDto> low = books.lowStock(5, PageRequest.of(0, 6)).stream()
            .map(b -> new LowStockDto(b.getId(), b.getSlug(), b.getTitle(), b.getStockQuantity())).toList();
        List<TopSellerDto> top = books.bestSellers(PageRequest.of(0, 5)).stream()
            .map(b -> new TopSellerDto(b.getId(), b.getSlug(), b.getTitle(), b.getSoldCount(), b.getCoverStyle())).toList();
        return new DashboardDto(orderRepo.countByPlacedAtAfter(startOfDay), orderRepo.countByPlacedAtAfter(last30), revenue,
                paid == 0 ? Money.ZERO : revenue.divide(BigDecimal.valueOf(paid), 2, RoundingMode.HALF_EVEN),
                orderRepo.countByStatus(OrderStatus.PENDING_PAYMENT), shipments.countByStatus(ShipmentStatus.PREPARING),
                shipments.countByStatus(ShipmentStatus.IN_TRANSIT) + shipments.countByStatus(ShipmentStatus.OUT_FOR_DELIVERY),
                users.count(), books.count(), byStatus, low, top);
    }

    // ---------- pedidos ----------

    @GetMapping("/orders")
    public PageDto<OrderSummaryDto> orders(@RequestParam(required = false) String status, @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return orders.adminList(status, q, page, size);
    }

    @GetMapping("/orders/{code}")
    public OrderDetailDto order(@PathVariable String code) {
        return orders.adminDetail(code);
    }

    @PostMapping("/orders/{code}/shipment/advance")
    public OrderDetailDto advance(@PathVariable String code, @Valid @RequestBody(required = false) AdvanceRequest req) {
        return orders.adminAdvance(code, req);
    }

    @PostMapping("/orders/{code}/cancel")
    public OrderDetailDto cancel(@PathVariable String code, @Valid @RequestBody(required = false) CancelRequest req) {
        return orders.adminCancel(code, req == null ? null : req.reason());
    }

    // ---------- livros ----------

    @GetMapping("/books")
    public PageDto<AdminBookDto> books(@RequestParam(required = false) String q, @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return catalog.books(q, category, page, size);
    }

    @GetMapping("/books/{id}")
    public AdminBookDto book(@PathVariable long id) {
        return catalog.book(id);
    }

    @PostMapping("/books")
    @ResponseStatus(HttpStatus.CREATED)
    public AdminBookDto create(@Valid @RequestBody BookRequest req) {
        return catalog.create(req);
    }

    @PutMapping("/books/{id}")
    public AdminBookDto update(@PathVariable long id, @Valid @RequestBody BookRequest req) {
        return catalog.update(id, req);
    }

    @PatchMapping("/books/{id}/stock")
    public AdminBookDto stock(@PathVariable long id, @Valid @RequestBody StockRequest req) {
        return catalog.stock(id, req.stockQuantity());
    }

    @DeleteMapping("/books/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deactivate(@PathVariable long id) {
        catalog.deactivate(id);
    }

    // ---------- categorias e catálogos ----------

    @GetMapping("/categories")
    public List<AdminCategoryDto> categories() {
        return catalog.categories();
    }

    @PostMapping("/categories")
    @ResponseStatus(HttpStatus.CREATED)
    public AdminCategoryDto createCategory(@Valid @RequestBody CategoryRequest req) {
        return catalog.createCategory(req);
    }

    @PutMapping("/categories/{id}")
    public AdminCategoryDto updateCategory(@PathVariable long id, @Valid @RequestBody CategoryRequest req) {
        return catalog.updateCategory(id, req);
    }

    @GetMapping("/catalogs")
    public List<AdminCatalogDto> catalogs() {
        return catalog.catalogs();
    }

    @PostMapping("/catalogs")
    @ResponseStatus(HttpStatus.CREATED)
    public AdminCatalogDto createCatalog(@Valid @RequestBody CatalogRequest req) {
        return catalog.createCatalog(req);
    }

    @PutMapping("/catalogs/{id}")
    public AdminCatalogDto updateCatalog(@PathVariable long id, @Valid @RequestBody CatalogRequest req) {
        return catalog.updateCatalog(id, req);
    }

    @DeleteMapping("/catalogs/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCatalog(@PathVariable long id) {
        catalog.deleteCatalog(id);
    }
}
