package io.github.violetpixel.bookstore.catalog;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import io.github.violetpixel.bookstore.common.Text;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;

/** Filtros da busca de livros, traduzidos para Criteria (portável entre os quatro bancos). */
public record BookSearch(String q, String category, String catalog, String format, BigDecimal minPrice, BigDecimal maxPrice,
        Boolean inStock, String sort) {

    public Specification<Book> toSpec(boolean includeInactive) {
        return (root, query, cb) -> {
            List<Predicate> where = new ArrayList<>();
            if (!includeInactive) {
                where.add(cb.equal(root.get("active"), Boolean.TRUE));
            }
            String folded = Text.fold(q);
            if (!folded.isBlank()) {
                for (String term : folded.split(" ")) {
                    where.add(cb.like(root.get("searchText"), "%" + term + "%"));
                }
            }
            if (category != null && !category.isBlank()) {
                where.add(cb.equal(root.get("category").get("slug"), category));
            }
            if (catalog != null && !catalog.isBlank()) {
                Subquery<Long> sq = query.subquery(Long.class);
                Root<CuratedCatalog> c = sq.from(CuratedCatalog.class);
                Join<CuratedCatalog, Book> books = c.join("books");
                sq.select(books.get("id")).where(cb.equal(c.get("slug"), catalog));
                where.add(root.get("id").in(sq));
            }
            if (format != null && !format.isBlank()) {
                try {
                    where.add(cb.equal(root.get("format"), BookFormat.valueOf(format.toUpperCase(Locale.ROOT))));
                } catch (IllegalArgumentException ignored) {
                    // formato desconhecido: ignora o filtro
                }
            }
            if (minPrice != null) {
                where.add(cb.greaterThanOrEqualTo(root.get("price"), minPrice));
            }
            if (maxPrice != null) {
                where.add(cb.lessThanOrEqualTo(root.get("price"), maxPrice));
            }
            if (Boolean.TRUE.equals(inStock)) {
                where.add(cb.greaterThan(root.get("stockQuantity"), 0));
            }
            return cb.and(where.toArray(Predicate[]::new));
        };
    }

    public Sort toSort() {
        String key = sort == null ? "" : sort;
        return switch (key) {
            case "recent" -> Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("id"));
            case "price_asc" -> Sort.by(Sort.Order.asc("price"), Sort.Order.asc("id"));
            case "price_desc" -> Sort.by(Sort.Order.desc("price"), Sort.Order.asc("id"));
            case "title" -> Sort.by(Sort.Order.asc("title"), Sort.Order.asc("id"));
            case "rating" -> Sort.by(Sort.Order.desc("ratingAverage"), Sort.Order.desc("ratingCount"), Sort.Order.asc("id"));
            default -> Sort.by(Sort.Order.desc("soldCount"), Sort.Order.asc("id"));
        };
    }
}
