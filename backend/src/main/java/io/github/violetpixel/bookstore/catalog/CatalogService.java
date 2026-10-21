package io.github.violetpixel.bookstore.catalog;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.github.violetpixel.bookstore.catalog.CatalogDtos.AuthorDto;
import io.github.violetpixel.bookstore.catalog.CatalogDtos.BookCard;
import io.github.violetpixel.bookstore.catalog.CatalogDtos.BookDetail;
import io.github.violetpixel.bookstore.catalog.CatalogDtos.CatalogDto;
import io.github.violetpixel.bookstore.catalog.CatalogDtos.CategoryDto;
import io.github.violetpixel.bookstore.catalog.CatalogDtos.CategoryRef;
import io.github.violetpixel.bookstore.catalog.CatalogDtos.HomeDto;
import io.github.violetpixel.bookstore.common.ApiException;
import io.github.violetpixel.bookstore.common.PageDto;
import io.github.violetpixel.bookstore.config.CacheConfig;

@Service
@Transactional(readOnly = true)
public class CatalogService {

    private static final int MAX_PAGE_SIZE = 48;

    private final BookRepository books;
    private final CategoryRepository categories;
    private final CuratedCatalogRepository catalogs;

    public CatalogService(BookRepository books, CategoryRepository categories, CuratedCatalogRepository catalogs) {
        this.books = books;
        this.categories = categories;
        this.catalogs = catalogs;
    }

    public PageDto<BookCard> search(BookSearch search, int page, int size) {
        PageRequest pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), MAX_PAGE_SIZE), search.toSort());
        Page<Book> result = books.findAll(search.toSpec(false), pageable);
        return PageDto.of(result, BookCard::of);
    }

    @Cacheable(cacheNames = CacheConfig.BOOK, key = "#slug")
    public BookDetail book(String slug) {
        Book b = books.findBySlug(slug).filter(Book::isActive).orElseThrow(() -> ApiException.notFound("Livro"));
        List<BookCard> related = books.related(b.getCategory().getId(), b.getId(), PageRequest.of(0, 8)).stream()
            .map(BookCard::of).toList();
        List<AuthorDto> authors = b.getAuthors().stream().map(a -> new AuthorDto(a.getName(), a.getSlug(), a.getBio())).toList();
        return new BookDetail(b.getId(), b.getSlug(), b.getIsbn(), b.getTitle(), b.getSubtitle(), b.getSynopsis(),
                List.copyOf(authors), b.getPublisher().getName(), CategoryRef.of(b.getCategory()), b.getLanguageCode(),
                b.getPageCount(), b.getPublishedYear(), b.getFormat().name(), b.getPrice(), b.getListPrice(),
                b.getStockQuantity(), b.getWeightGrams(), b.getCoverStyle(), b.getRatingAverage(), b.getRatingCount(),
                b.inStock(), List.copyOf(related));
    }

    @Cacheable(cacheNames = CacheConfig.CATEGORIES, key = "'all'")
    public List<CategoryDto> categories() {
        Map<Long, Long> counts = new HashMap<>();
        for (Object[] row : categories.countActiveBooksByCategory()) {
            counts.put((Long) row[0], ((Number) row[1]).longValue());
        }
        return List.copyOf(categories.findAllByOrderBySortOrderAscNameAsc().stream()
            .filter(Category::isActive)
            .map(c -> new CategoryDto(c.getId(), c.getName(), c.getSlug(), c.getDescription(), c.getAccent(),
                    counts.getOrDefault(c.getId(), 0L)))
            .toList());
    }

    public CategoryDto category(String slug) {
        return categories().stream().filter(c -> c.slug().equals(slug)).findFirst()
            .orElseThrow(() -> ApiException.notFound("Categoria"));
    }

    @Cacheable(cacheNames = CacheConfig.CATALOGS, key = "'all'")
    public List<CatalogDto> catalogs() {
        return List.copyOf(catalogs.findAllByOrderBySortOrderAscNameAsc().stream()
            .filter(CuratedCatalog::isActive)
            .map(c -> toDto(c, 12))
            .toList());
    }

    @Cacheable(cacheNames = CacheConfig.CATALOGS, key = "#slug")
    public CatalogDto catalog(String slug) {
        CuratedCatalog c = catalogs.findBySlug(slug).filter(CuratedCatalog::isActive)
            .orElseThrow(() -> ApiException.notFound("Catálogo"));
        return toDto(c, 60);
    }

    @Cacheable(cacheNames = CacheConfig.HOME, key = "'home'")
    public HomeDto home() {
        List<BookCard> spotlight = books.bestSellers(PageRequest.of(0, 40)).stream()
            .filter(Book::inStock)
            .sorted(Comparator.comparing(Book::getRatingAverage).reversed())
            .limit(3).map(BookCard::of).toList();
        return new HomeDto(catalogs(), categories(), List.copyOf(spotlight));
    }

    private static CatalogDto toDto(CuratedCatalog c, int limit) {
        List<Book> active = c.getBooks().stream().filter(b -> b != null && b.isActive()).toList();
        List<BookCard> cards = active.stream().limit(limit).map(BookCard::of).toList();
        return new CatalogDto(c.getId(), c.getName(), c.getSlug(), c.getDescription(), c.getHighlight(), active.size(),
                List.copyOf(cards));
    }
}
