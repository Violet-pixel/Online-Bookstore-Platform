package io.github.violetpixel.bookstore.common;

import java.util.List;
import java.util.function.Function;

import org.springframework.data.domain.Page;

public record PageDto<T>(List<T> items, int page, int size, long total, int totalPages) {

    public static <E, T> PageDto<T> of(Page<E> page, Function<E, T> mapper) {
        return new PageDto<>(page.getContent().stream().map(mapper).toList(), page.getNumber(), page.getSize(),
                page.getTotalElements(), page.getTotalPages());
    }
}
