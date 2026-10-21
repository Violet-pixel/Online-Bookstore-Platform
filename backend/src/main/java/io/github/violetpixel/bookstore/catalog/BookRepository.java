package io.github.violetpixel.bookstore.catalog;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BookRepository extends JpaRepository<Book, Long>, JpaSpecificationExecutor<Book> {

    @EntityGraph(attributePaths = {"category", "publisher"})
    Optional<Book> findBySlug(String slug);

    boolean existsByIsbn(String isbn);

    boolean existsBySlug(String slug);

    @EntityGraph(attributePaths = {"category", "publisher"})
    List<Book> findByIdIn(Collection<Long> ids);

    @EntityGraph(attributePaths = {"category", "publisher"})
    @Query("select b from Book b where b.category.id = :categoryId and b.id <> :bookId and b.active = true order by b.soldCount desc, b.id")
    List<Book> related(@Param("categoryId") Long categoryId, @Param("bookId") Long bookId, Pageable page);

    @Query("select b from Book b where b.active = true and b.stockQuantity <= :limit order by b.stockQuantity, b.title")
    List<Book> lowStock(@Param("limit") int limit, Pageable page);

    @Query("select b from Book b where b.active = true order by b.soldCount desc, b.id")
    List<Book> bestSellers(Pageable page);

    /** Reserva estoque de forma atômica: só baixa se houver quantidade suficiente. */
    @Modifying(flushAutomatically = true)
    @Query("update Book b set b.stockQuantity = b.stockQuantity - :qty, b.soldCount = b.soldCount + :qty "
            + "where b.id = :id and b.stockQuantity >= :qty")
    int reserve(@Param("id") Long id, @Param("qty") int qty);

    @Modifying(flushAutomatically = true)
    @Query("update Book b set b.stockQuantity = b.stockQuantity + :qty, b.soldCount = b.soldCount - :qty where b.id = :id")
    int release(@Param("id") Long id, @Param("qty") int qty);
}
