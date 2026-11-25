package com.cadence.common.application;

import java.util.List;
import java.util.function.Function;

/**
 * One page of results, as a port returns it. Plain Java instead of Spring Data's {@code Page}, so
 * the application layer stays free of persistence types (ArchUnit R9); the controller turns it into
 * {@code UIPageResponse}.
 *
 * @param content the items of this page
 * @param page page number, starting at 1 like the API
 * @param size requested page size
 * @param totalElements items across all pages
 */
public record PageResult<T>(List<T> content, int page, int size, long totalElements) {

    public PageResult {
        content = List.copyOf(content);
        if (page < 1) {
            throw new IllegalArgumentException("page starts at 1, got " + page);
        }
        if (size < 1) {
            throw new IllegalArgumentException("size must be positive, got " + size);
        }
        if (totalElements < 0) {
            throw new IllegalArgumentException("totalElements must not be negative");
        }
    }

    public static <T> PageResult<T> of(List<T> content, int page, int size, long totalElements) {
        return new PageResult<>(content, page, size, totalElements);
    }

    /** Pages needed for every item; 0 when there is nothing to show. */
    public int totalPages() {
        return (int) ((totalElements + size - 1) / size);
    }

    /** Same page with every item converted, for example a snapshot into a view. */
    public <R> PageResult<R> map(Function<? super T, ? extends R> mapper) {
        List<R> mapped = content.stream().<R>map(mapper).toList();
        return new PageResult<>(mapped, page, size, totalElements);
    }
}
