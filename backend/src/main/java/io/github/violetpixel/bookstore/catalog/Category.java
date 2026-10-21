package io.github.violetpixel.bookstore.catalog;

import io.github.violetpixel.bookstore.common.Flag;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "category")
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 80)
    private String name;

    @Column(nullable = false, unique = true, length = 90)
    private String slug;

    @Column(length = 400)
    private String description;

    @Column(nullable = false, length = 7)
    private String accent;

    @Column(nullable = false)
    private Integer sortOrder = 0;

    @Convert(converter = Flag.class)
    @Column(nullable = false)
    private Boolean active = Boolean.TRUE;

    protected Category() { }

    public Category(String name, String slug, String description, String accent, int sortOrder) {
        this.name = name;
        this.slug = slug;
        this.description = description;
        this.accent = accent;
        this.sortOrder = sortOrder;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getSlug() { return slug; }
    public void setSlug(String slug) { this.slug = slug; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getAccent() { return accent; }
    public void setAccent(String accent) { this.accent = accent; }
    public int getSortOrder() { return sortOrder; }
    public void setSortOrder(int sortOrder) { this.sortOrder = sortOrder; }
    public boolean isActive() { return Boolean.TRUE.equals(active); }
    public void setActive(boolean active) { this.active = active; }
}
