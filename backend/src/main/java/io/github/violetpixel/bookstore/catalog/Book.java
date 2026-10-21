package io.github.violetpixel.bookstore.catalog;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import io.github.violetpixel.bookstore.common.Flag;
import io.github.violetpixel.bookstore.common.Text;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name = "book")
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 17)
    private String isbn;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(length = 200)
    private String subtitle;

    @Column(nullable = false, unique = true, length = 220)
    private String slug;

    @Column(length = 4000)
    private String synopsis;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "publisher_id", nullable = false)
    private Publisher publisher;

    @ManyToMany
    @JoinTable(name = "book_author",
            joinColumns = @JoinColumn(name = "book_id"),
            inverseJoinColumns = @JoinColumn(name = "author_id"))
    @OrderColumn(name = "author_order")
    private List<Author> authors = new ArrayList<>();

    @Column(nullable = false, length = 5)
    private String languageCode = "pt-BR";

    private Integer pageCount;

    private Integer publishedYear;

    @Enumerated(EnumType.STRING)
    @Column(name = "book_format", nullable = false, length = 20)
    private BookFormat format = BookFormat.PAPERBACK;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal listPrice;

    @Column(nullable = false)
    private Integer stockQuantity = 0;

    @Column(nullable = false)
    private Integer weightGrams = 400;

    @Column(nullable = false)
    private Integer coverStyle = 0;

    @Column(nullable = false, precision = 3, scale = 2)
    private BigDecimal ratingAverage = BigDecimal.ZERO;

    @Column(nullable = false)
    private Integer ratingCount = 0;

    @Column(nullable = false)
    private Integer soldCount = 0;

    @Convert(converter = Flag.class)
    @Column(nullable = false)
    private Boolean active = Boolean.TRUE;

    @Column(nullable = false, length = 1000)
    private String searchText = "";

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    protected Book() { }

    public Book(String isbn, String title, String slug, Category category, Publisher publisher) {
        this.isbn = isbn;
        this.title = title;
        this.slug = slug;
        this.category = category;
        this.publisher = publisher;
    }

    @PrePersist
    void prePersist() {
        Instant now = Instant.now();
        if (createdAt == null) {
            createdAt = now;
        }
        updatedAt = now;
        refreshSearchText();
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = Instant.now();
    }

    /** Recalcula a coluna de busca (sem acentos): título, subtítulo, autores, editora e ISBN. */
    public void refreshSearchText() {
        String names = String.join(" ", authors.stream().map(Author::getName).toList());
        this.searchText = Text.searchable(title, subtitle, names, publisher != null ? publisher.getName() : null,
                isbn, isbn != null ? isbn.replace("-", "") : null);
    }

    public boolean inStock() {
        return isActive() && stockQuantity > 0;
    }

    public Long getId() { return id; }
    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getSubtitle() { return subtitle; }
    public void setSubtitle(String subtitle) { this.subtitle = subtitle; }
    public String getSlug() { return slug; }
    public void setSlug(String slug) { this.slug = slug; }
    public String getSynopsis() { return synopsis; }
    public void setSynopsis(String synopsis) { this.synopsis = synopsis; }
    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }
    public Publisher getPublisher() { return publisher; }
    public void setPublisher(Publisher publisher) { this.publisher = publisher; }
    public List<Author> getAuthors() { return authors; }
    public String getLanguageCode() { return languageCode; }
    public void setLanguageCode(String languageCode) { this.languageCode = languageCode; }
    public Integer getPageCount() { return pageCount; }
    public void setPageCount(Integer pageCount) { this.pageCount = pageCount; }
    public Integer getPublishedYear() { return publishedYear; }
    public void setPublishedYear(Integer publishedYear) { this.publishedYear = publishedYear; }
    public BookFormat getFormat() { return format; }
    public void setFormat(BookFormat format) { this.format = format; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public BigDecimal getListPrice() { return listPrice; }
    public void setListPrice(BigDecimal listPrice) { this.listPrice = listPrice; }
    public int getStockQuantity() { return stockQuantity; }
    public void setStockQuantity(int stockQuantity) { this.stockQuantity = stockQuantity; }
    public int getWeightGrams() { return weightGrams; }
    public void setWeightGrams(int weightGrams) { this.weightGrams = weightGrams; }
    public int getCoverStyle() { return coverStyle; }
    public void setCoverStyle(int coverStyle) { this.coverStyle = coverStyle; }
    public BigDecimal getRatingAverage() { return ratingAverage; }
    public void setRatingAverage(BigDecimal ratingAverage) { this.ratingAverage = ratingAverage; }
    public int getRatingCount() { return ratingCount; }
    public void setRatingCount(int ratingCount) { this.ratingCount = ratingCount; }
    public int getSoldCount() { return soldCount; }
    public void setSoldCount(int soldCount) { this.soldCount = soldCount; }
    public boolean isActive() { return Boolean.TRUE.equals(active); }
    public void setActive(boolean active) { this.active = active; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
