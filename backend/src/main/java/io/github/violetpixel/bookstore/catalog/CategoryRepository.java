package io.github.violetpixel.bookstore.catalog;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    Optional<Category> findBySlug(String slug);

    List<Category> findAllByOrderBySortOrderAscNameAsc();

    @Query("select b.category.id, count(b) from Book b where b.active = true group by b.category.id")
    List<Object[]> countActiveBooksByCategory();
}
