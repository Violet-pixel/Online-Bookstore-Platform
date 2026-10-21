package io.github.violetpixel.bookstore.catalog;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CuratedCatalogRepository extends JpaRepository<CuratedCatalog, Long> {

    Optional<CuratedCatalog> findBySlug(String slug);

    List<CuratedCatalog> findAllByOrderBySortOrderAscNameAsc();
}
