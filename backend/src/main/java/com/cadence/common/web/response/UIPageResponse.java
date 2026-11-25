package com.cadence.common.web.response;

import java.util.List;
import java.util.function.Function;

import com.cadence.common.application.PageResult;

/**
 * Body of every paged list. {@code number} starts at 1, like the {@code page} request parameter, so
 * the client never converts.
 */
public record UIPageResponse<T>(
        int number,
        int size,
        long totalElements,
        int totalPages,
        int numberOfElements,
        List<T> content) {

    public UIPageResponse {
        content = List.copyOf(content);
    }

    /** A page whose items are already response records. */
    public static <T> UIPageResponse<T> from(PageResult<T> page) {
        return from(page, Function.identity());
    }

    /** Converts each item, typically {@code ProductTileResponse::from}. */
    public static <V, T> UIPageResponse<T> from(
            PageResult<V> page, Function<? super V, ? extends T> mapper) {
        List<T> content = page.content().stream().<T>map(mapper).toList();
        return new UIPageResponse<>(
                page.page(),
                page.size(),
                page.totalElements(),
                page.totalPages(),
                content.size(),
                content);
    }
}
