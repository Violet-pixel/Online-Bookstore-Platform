package io.github.violetpixel.bookstore.admin;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.github.violetpixel.bookstore.admin.AdminDtos.AdminBookDto;
import io.github.violetpixel.bookstore.admin.AdminDtos.AdminCatalogDto;
import io.github.violetpixel.bookstore.admin.AdminDtos.AdminCategoryDto;
import io.github.violetpixel.bookstore.admin.AdminDtos.BookRequest;
import io.github.violetpixel.bookstore.admin.AdminDtos.CatalogRequest;
import io.github.violetpixel.bookstore.admin.AdminDtos.CategoryRequest;
import io.github.violetpixel.bookstore.catalog.Author;
import io.github.violetpixel.bookstore.catalog.AuthorRepository;
import io.github.violetpixel.bookstore.catalog.Book;
import io.github.violetpixel.bookstore.catalog.BookFormat;
import io.github.violetpixel.bookstore.catalog.BookRepository;
import io.github.violetpixel.bookstore.catalog.BookSearch;
import io.github.violetpixel.bookstore.catalog.CatalogCache;
import io.github.violetpixel.bookstore.catalog.Category;
import io.github.violetpixel.bookstore.catalog.CategoryRepository;
import io.github.violetpixel.bookstore.catalog.CuratedCatalog;
import io.github.violetpixel.bookstore.catalog.CuratedCatalogRepository;
import io.github.violetpixel.bookstore.catalog.Publisher;
import io.github.violetpixel.bookstore.catalog.PublisherRepository;
import io.github.violetpixel.bookstore.common.ApiException;
import io.github.violetpixel.bookstore.common.Money;
import io.github.violetpixel.bookstore.common.PageDto;
import io.github.violetpixel.bookstore.common.Text;

@Service
public class AdminCatalogService {

    private final BookRepository books;
    private final CategoryRepository categories;
    private final CuratedCatalogRepository catalogs;
    private final AuthorRepository authors;
    private final PublisherRepository publishers;
    private final CatalogCache cache;

    public AdminCatalogService(BookRepository books, CategoryRepository categories, CuratedCatalogRepository catalogs,
            AuthorRepository authors, PublisherRepository publishers, CatalogCache cache) {
        this.books = books;
        this.categories = categories;
        this.catalogs = catalogs;
        this.authors = authors;
        this.publishers = publishers;
        this.cache = cache;
    }

    // ---------- livros ----------

    @Transactional(readOnly = true)
    public PageDto<AdminBookDto> books(String q, String category, int page, int size) {
        BookSearch search = new BookSearch(q, category, null, null, null, null, null, "title");
        PageRequest pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100),
                Sort.by(Sort.Order.asc("title"), Sort.Order.asc("id")));
        return PageDto.of(books.findAll(search.toSpec(true), pageable), AdminCatalogService::toDto);
    }

    @Transactional(readOnly = true)
    public AdminBookDto book(long id) {
        return toDto(books.findById(id).orElseThrow(() -> ApiException.notFound("Livro")));
    }

    @Transactional
    public AdminBookDto create(BookRequest req) {
        String isbn = req.isbn().trim();
        if (books.existsByIsbn(isbn)) {
            throw ApiException.conflict("isbn_em_uso", "Já existe um livro com este ISBN.");
        }
        Book book = new Book(isbn, req.title().trim(), uniqueSlug(req.title(), null), category(req.categorySlug()),
                publisher(req.publisher()));
        apply(book, req);
        book = books.save(book);
        cache.evictAll();
        return toDto(book);
    }

    @Transactional
    public AdminBookDto update(long id, BookRequest req) {
        Book book = books.findById(id).orElseThrow(() -> ApiException.notFound("Livro"));
        String isbn = req.isbn().trim();
        if (!isbn.equals(book.getIsbn()) && books.existsByIsbn(isbn)) {
            throw ApiException.conflict("isbn_em_uso", "Já existe um livro com este ISBN.");
        }
        book.setIsbn(isbn);
        if (!book.getTitle().equals(req.title().trim())) {
            book.setTitle(req.title().trim());
            book.setSlug(uniqueSlug(req.title(), book.getId()));
        }
        book.setCategory(category(req.categorySlug()));
        book.setPublisher(publisher(req.publisher()));
        apply(book, req);
        cache.evictAll();
        return toDto(book);
    }

    @Transactional
    public AdminBookDto stock(long id, int quantity) {
        Book book = books.findById(id).orElseThrow(() -> ApiException.notFound("Livro"));
        book.setStockQuantity(quantity);
        cache.evictBooks(List.of(book.getSlug()));
        return toDto(book);
    }

    @Transactional
    public void deactivate(long id) {
        Book book = books.findById(id).orElseThrow(() -> ApiException.notFound("Livro"));
        book.setActive(false);
        cache.evictAll();
    }

    private void apply(Book book, BookRequest req) {
        book.setSubtitle(blank(req.subtitle()));
        book.setSynopsis(blank(req.synopsis()));
        book.setLanguageCode(req.languageCode() == null || req.languageCode().isBlank() ? "pt-BR" : req.languageCode().trim());
        book.setPageCount(req.pageCount());
        book.setPublishedYear(req.publishedYear());
        try {
            book.setFormat(BookFormat.valueOf(req.format().trim().toUpperCase(Locale.ROOT)));
        } catch (IllegalArgumentException e) {
            throw ApiException.badRequest("formato_invalido", "Formato desconhecido.");
        }
        if (req.listPrice().compareTo(req.price()) < 0) {
            throw ApiException.badRequest("preco_invalido", "O preço de capa não pode ser menor que o preço de venda.");
        }
        book.setPrice(Money.scale(req.price()));
        book.setListPrice(Money.scale(req.listPrice()));
        book.setStockQuantity(req.stockQuantity());
        book.setWeightGrams(req.weightGrams());
        book.setCoverStyle(req.coverStyle());
        book.setActive(req.active());
        book.getAuthors().clear();
        for (String name : req.authors()) {
            book.getAuthors().add(author(name));
        }
        book.refreshSearchText();
    }

    private Author author(String name) {
        String clean = name.trim();
        String slug = Text.slug(clean);
        return authors.findBySlug(slug).orElseGet(() -> authors.save(new Author(clean, slug, null)));
    }

    private Publisher publisher(String name) {
        String clean = name.trim();
        String slug = Text.slug(clean);
        return publishers.findBySlug(slug).orElseGet(() -> publishers.save(new Publisher(clean, slug)));
    }

    private Category category(String slug) {
        return categories.findBySlug(slug).orElseThrow(() -> ApiException.badRequest("categoria_invalida", "Categoria não encontrada."));
    }

    private String uniqueSlug(String title, Long selfId) {
        String base = Text.slug(title);
        String slug = base;
        int n = 2;
        while (books.findBySlug(slug).filter(b -> !b.getId().equals(selfId)).isPresent()) {
            slug = base + "-" + n++;
        }
        return slug;
    }

    static AdminBookDto toDto(Book b) {
        return new AdminBookDto(b.getId(), b.getSlug(), b.getIsbn(), b.getTitle(), b.getSubtitle(), b.getSynopsis(),
                b.getAuthors().stream().map(Author::getName).toList(), b.getPublisher().getName(), b.getCategory().getSlug(),
                b.getCategory().getName(), b.getLanguageCode(), b.getPageCount(), b.getPublishedYear(), b.getFormat().name(),
                b.getPrice(), b.getListPrice(), b.getStockQuantity(), b.getWeightGrams(), b.getCoverStyle(), b.getSoldCount(),
                b.isActive());
    }

    // ---------- categorias ----------

    @Transactional(readOnly = true)
    public List<AdminCategoryDto> categories() {
        return categories.findAllByOrderBySortOrderAscNameAsc().stream().map(AdminCatalogService::toDto).toList();
    }

    @Transactional
    public AdminCategoryDto createCategory(CategoryRequest req) {
        String slug = Text.slug(req.name());
        if (categories.findBySlug(slug).isPresent()) {
            throw ApiException.conflict("categoria_existe", "Já existe uma categoria com esse nome.");
        }
        Category c = new Category(req.name().trim(), slug, blank(req.description()), req.accent().toUpperCase(Locale.ROOT),
                req.sortOrder());
        c.setActive(req.active());
        c = categories.save(c);
        cache.evictAll();
        return toDto(c);
    }

    @Transactional
    public AdminCategoryDto updateCategory(long id, CategoryRequest req) {
        Category c = categories.findById(id).orElseThrow(() -> ApiException.notFound("Categoria"));
        c.setName(req.name().trim());
        c.setDescription(blank(req.description()));
        c.setAccent(req.accent().toUpperCase(Locale.ROOT));
        c.setSortOrder(req.sortOrder());
        c.setActive(req.active());
        cache.evictAll();
        return toDto(c);
    }

    static AdminCategoryDto toDto(Category c) {
        return new AdminCategoryDto(c.getId(), c.getName(), c.getSlug(), c.getDescription(), c.getAccent(), c.getSortOrder(),
                c.isActive());
    }

    // ---------- catálogos ----------

    @Transactional(readOnly = true)
    public List<AdminCatalogDto> catalogs() {
        return catalogs.findAllByOrderBySortOrderAscNameAsc().stream().map(AdminCatalogService::toDto).toList();
    }

    @Transactional
    public AdminCatalogDto createCatalog(CatalogRequest req) {
        String slug = Text.slug(req.name());
        if (catalogs.findBySlug(slug).isPresent()) {
            throw ApiException.conflict("catalogo_existe", "Já existe um catálogo com esse nome.");
        }
        CuratedCatalog c = new CuratedCatalog(req.name().trim(), slug, blank(req.description()), blank(req.highlight()),
                req.sortOrder());
        c.setActive(req.active());
        setBooks(c, req.bookIds());
        c = catalogs.save(c);
        cache.evictAll();
        return toDto(c);
    }

    @Transactional
    public AdminCatalogDto updateCatalog(long id, CatalogRequest req) {
        CuratedCatalog c = catalogs.findById(id).orElseThrow(() -> ApiException.notFound("Catálogo"));
        c.setName(req.name().trim());
        c.setDescription(blank(req.description()));
        c.setHighlight(blank(req.highlight()));
        c.setSortOrder(req.sortOrder());
        c.setActive(req.active());
        if (req.bookIds() != null) {
            setBooks(c, req.bookIds());
        }
        cache.evictAll();
        return toDto(c);
    }

    @Transactional
    public void deleteCatalog(long id) {
        catalogs.findById(id).ifPresent(catalogs::delete);
        cache.evictAll();
    }

    private void setBooks(CuratedCatalog c, List<Long> ids) {
        c.getBooks().clear();
        if (ids == null) {
            return;
        }
        List<Long> unique = new ArrayList<>(new java.util.LinkedHashSet<>(ids));
        var found = books.findAllById(unique);
        for (Long id : unique) {
            found.stream().filter(b -> b.getId().equals(id)).findFirst().ifPresent(c.getBooks()::add);
        }
    }

    static AdminCatalogDto toDto(CuratedCatalog c) {
        return new AdminCatalogDto(c.getId(), c.getName(), c.getSlug(), c.getDescription(), c.getHighlight(), c.getSortOrder(),
                c.isActive(), c.getBooks().stream().map(Book::getId).toList());
    }

    private static String blank(String v) {
        return v == null || v.isBlank() ? null : v.trim();
    }
}
