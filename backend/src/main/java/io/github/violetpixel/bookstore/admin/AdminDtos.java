package io.github.violetpixel.bookstore.admin;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public final class AdminDtos {

    private AdminDtos() { }

    public record BookRequest(
            @NotBlank(message = "Informe o ISBN.") @Pattern(regexp = "[0-9-]{10,17}", message = "ISBN inválido.") String isbn,
            @NotBlank(message = "Informe o título.") @Size(max = 200) String title,
            @Size(max = 200) String subtitle,
            @Size(max = 4000) String synopsis,
            @NotEmpty(message = "Informe ao menos um autor.") @Size(max = 6) List<@NotBlank @Size(max = 120) String> authors,
            @NotBlank(message = "Informe a editora.") @Size(max = 120) String publisher,
            @NotBlank(message = "Escolha a categoria.") String categorySlug,
            @Size(max = 5) String languageCode,
            @Min(1) @Max(5000) Integer pageCount,
            @Min(1450) @Max(2100) Integer publishedYear,
            @NotBlank String format,
            @NotNull @DecimalMin(value = "0.01", message = "Preço inválido.") BigDecimal price,
            @NotNull @DecimalMin(value = "0.01", message = "Preço inválido.") BigDecimal listPrice,
            @Min(0) @Max(100_000) int stockQuantity,
            @Min(1) @Max(20_000) int weightGrams,
            @Min(0) @Max(11) int coverStyle,
            boolean active) {
    }

    public record AdminBookDto(long id, String slug, String isbn, String title, String subtitle, String synopsis,
            List<String> authors, String publisher, String categorySlug, String categoryName, String languageCode,
            Integer pageCount, Integer publishedYear, String format, BigDecimal price, BigDecimal listPrice,
            int stockQuantity, int weightGrams, int coverStyle, int soldCount, boolean active) {
    }

    public record StockRequest(@Min(0) @Max(100_000) int stockQuantity) {
    }

    public record CategoryRequest(@NotBlank @Size(max = 80) String name, @Size(max = 400) String description,
            @NotBlank @Pattern(regexp = "#[0-9A-Fa-f]{6}", message = "Cor inválida.") String accent,
            @Min(0) @Max(999) int sortOrder, boolean active) {
    }

    public record AdminCategoryDto(long id, String name, String slug, String description, String accent, int sortOrder,
            boolean active) {
    }

    public record CatalogRequest(@NotBlank @Size(max = 80) String name, @Size(max = 400) String description,
            @Size(max = 160) String highlight, @Min(0) @Max(999) int sortOrder, boolean active,
            @Size(max = 120) List<Long> bookIds) {
    }

    public record AdminCatalogDto(long id, String name, String slug, String description, String highlight, int sortOrder,
            boolean active, List<Long> bookIds) {
    }

    public record LowStockDto(long id, String slug, String title, int stockQuantity) {
    }

    public record TopSellerDto(long id, String slug, String title, int soldCount, int coverStyle) {
    }

    public record DashboardDto(long ordersToday, long ordersLast30, BigDecimal revenueLast30, BigDecimal averageTicket,
            long awaitingPayment, long toShip, long inTransit, long customers, long activeBooks,
            Map<String, Long> ordersByStatus, List<LowStockDto> lowStock, List<TopSellerDto> topSellers) {
    }
}
