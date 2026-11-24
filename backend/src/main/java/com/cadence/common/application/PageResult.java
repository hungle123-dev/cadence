package com.cadence.common.application;

import com.cadence.common.domain.util.Preconditions;

import java.util.List;

/**
 * A page without Spring Data, so ports stay framework-free (template rule R4). Page numbers start at 1.
 */
public record PageResult<T>(List<T> content, int page, int size, long totalElements) {

    public PageResult {
        content = List.copyOf(Preconditions.requireNonNull(content, "content"));
        if (page < 1) throw new IllegalArgumentException("page must be >= 1");
        if (size < 0) throw new IllegalArgumentException("size must be >= 0");
        if (totalElements < 0) throw new IllegalArgumentException("totalElements must be >= 0");
    }

    public int totalPages() {
        return size == 0 ? 0 : (int) Math.ceil((double) totalElements / size);
    }
}
