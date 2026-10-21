package io.github.violetpixel.bookstore.catalog;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/** Respostas do catálogo. São serializáveis porque vão para o cache no Redis. */
public final class CatalogDtos {

    private CatalogDtos() { }

    public record CategoryRef(String name, String slug, String accent) implements Serializable {
        public static CategoryRef of(Category c) {
            return new CategoryRef(c.getName(), c.getSlug(), c.getAccent());
        }
    }

    public record BookCard(long id, String slug, String title, String subtitle, List<String> authors, BigDecimal price,
            BigDecimal listPrice, String format, int coverStyle, CategoryRef category, BigDecimal rating, int ratingCount,
            boolean inStock) implements Serializable {

        public static BookCard of(Book b) {
            return new BookCard(b.getId(), b.getSlug(), b.getTitle(), b.getSubtitle(),
                    List.copyOf(b.getAuthors().stream().map(Author::getName).toList()), b.getPrice(), b.getListPrice(),
                    b.getFormat().name(), b.getCoverStyle(), CategoryRef.of(b.getCategory()), b.getRatingAverage(),
                    b.getRatingCount(), b.inStock());
        }
    }

    public record AuthorDto(String name, String slug, String bio) implements Serializable {
    }

    public record BookDetail(long id, String slug, String isbn, String title, String subtitle, String synopsis,
            List<AuthorDto> authors, String publisher, CategoryRef category, String language, Integer pageCount,
            Integer publishedYear, String format, BigDecimal price, BigDecimal listPrice, int stockQuantity, int weightGrams,
            int coverStyle, BigDecimal rating, int ratingCount, boolean inStock, List<BookCard> related) implements Serializable {
    }

    public record CategoryDto(long id, String name, String slug, String description, String accent, long bookCount)
            implements Serializable {
    }

    public record CatalogDto(long id, String name, String slug, String description, String highlight, int bookCount,
            List<BookCard> books) implements Serializable {
    }

    public record HomeDto(List<CatalogDto> catalogs, List<CategoryDto> categories, List<BookCard> spotlight)
            implements Serializable {
    }
}
