package io.github.violetpixel.bookstore.catalog;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PublisherRepository extends JpaRepository<Publisher, Long> {

    Optional<Publisher> findBySlug(String slug);
}
