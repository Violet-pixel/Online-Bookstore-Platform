package io.github.violetpixel.bookstore.catalog;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "author")
public class Author {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(nullable = false, unique = true, length = 130)
    private String slug;

    @Column(length = 1000)
    private String bio;

    protected Author() { }

    public Author(String name, String slug, String bio) {
        this.name = name;
        this.slug = slug;
        this.bio = bio;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getSlug() { return slug; }
    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }
}
