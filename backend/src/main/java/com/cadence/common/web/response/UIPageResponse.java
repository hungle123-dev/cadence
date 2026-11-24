package com.cadence.common.web.response;

import com.cadence.common.application.PageResult;

import java.util.List;
import java.util.function.Function;

/** Paged list; {@code number} starts at 1 like the page parameter. */
public record UIPageResponse<T>(int number, int size, long totalElements, int totalPages,
                                int numberOfElements, List<T> content) {

    public static <V, T> UIPageResponse<T> from(PageResult<V> page, Function<? super V, ? extends T> mapper) {
        List<T> content = page.content().stream().<T>map(mapper).toList();
        return new UIPageResponse<>(page.page(), page.size(), page.totalElements(),
                page.totalPages(), content.size(), content);
    }
}
