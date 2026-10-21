package io.github.violetpixel.bookstore.catalog;

import java.util.ArrayList;
import java.util.List;

import io.github.violetpixel.bookstore.common.Flag;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;

/** Catálogo curado: uma vitrine com livros em ordem própria ("Mais vendidos", "Lançamentos"...). */
@Entity
@Table(name = "curated_catalog")
public class CuratedCatalog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 80)
    private String name;

    @Column(nullable = false, unique = true, length = 90)
    private String slug;

    @Column(length = 400)
    private String description;

    @Column(length = 160)
    private String highlight;

    @Column(nullable = false)
    private Integer sortOrder = 0;

    @Convert(converter = Flag.class)
    @Column(nullable = false)
    private Boolean active = Boolean.TRUE;

    @ManyToMany
    @JoinTable(name = "curated_catalog_book",
            joinColumns = @JoinColumn(name = "catalog_id"),
            inverseJoinColumns = @JoinColumn(name = "book_id"))
    @OrderColumn(name = "item_position")
    private List<Book> books = new ArrayList<>();

    protected CuratedCatalog() { }

    public CuratedCatalog(String name, String slug, String description, String highlight, int sortOrder) {
        this.name = name;
        this.slug = slug;
        this.description = description;
        this.highlight = highlight;
        this.sortOrder = sortOrder;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getSlug() { return slug; }
    public void setSlug(String slug) { this.slug = slug; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getHighlight() { return highlight; }
    public void setHighlight(String highlight) { this.highlight = highlight; }
    public int getSortOrder() { return sortOrder; }
    public void setSortOrder(int sortOrder) { this.sortOrder = sortOrder; }
    public boolean isActive() { return Boolean.TRUE.equals(active); }
    public void setActive(boolean active) { this.active = active; }
    public List<Book> getBooks() { return books; }
}
