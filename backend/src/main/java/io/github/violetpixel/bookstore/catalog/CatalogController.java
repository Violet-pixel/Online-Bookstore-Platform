package io.github.violetpixel.bookstore.catalog;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.github.violetpixel.bookstore.catalog.CatalogDtos.BookCard;
import io.github.violetpixel.bookstore.catalog.CatalogDtos.BookDetail;
import io.github.violetpixel.bookstore.catalog.CatalogDtos.CatalogDto;
import io.github.violetpixel.bookstore.catalog.CatalogDtos.CategoryDto;
import io.github.violetpixel.bookstore.catalog.CatalogDtos.HomeDto;
import io.github.violetpixel.bookstore.common.PageDto;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api")
@Tag(name = "Catálogo")
public class CatalogController {

    private final CatalogService catalog;

    public CatalogController(CatalogService catalog) {
        this.catalog = catalog;
    }

    @GetMapping("/home")
    public HomeDto home() {
        return catalog.home();
    }

    @GetMapping("/books")
    public PageDto<BookCard> books(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String catalog,
            @RequestParam(required = false) String format,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) Boolean inStock,
            @RequestParam(required = false) String sort,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "24") int size) {
        return this.catalog.search(new BookSearch(q, category, catalog, format, minPrice, maxPrice, inStock, sort), page, size);
    }

    @GetMapping("/books/{slug}")
    public BookDetail book(@PathVariable String slug) {
        return catalog.book(slug);
    }

    @GetMapping("/categories")
    public List<CategoryDto> categories() {
        return catalog.categories();
    }

    @GetMapping("/categories/{slug}")
    public CategoryDto category(@PathVariable String slug) {
        return catalog.category(slug);
    }

    @GetMapping("/catalogs")
    public List<CatalogDto> catalogs() {
        return catalog.catalogs();
    }

    @GetMapping("/catalogs/{slug}")
    public CatalogDto catalog(@PathVariable String slug) {
        return catalog.catalog(slug);
    }
}
