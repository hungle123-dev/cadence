package com.cadence.catalog.application.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.jspecify.annotations.Nullable;

import com.cadence.catalog.domain.exception.InvalidProductFilterException;
import com.cadence.catalog.domain.model.ColorFamily;
import com.cadence.catalog.domain.model.Gender;
import com.cadence.common.domain.error.InvalidParam;

/** PLP filters, sort and page (CAT-01, CAT-02). Rejects every wrong value at once (AC 2.11). */
public record ProductTileQuery(
        @Nullable String categorySlug,
        List<String> sizes,
        List<ColorFamily> colors,
        @Nullable Gender gender,
        @Nullable Long minPrice,
        @Nullable Long maxPrice,
        ProductSort sort,
        int page) {

    /** Fixed page size (A2). */
    public static final int PAGE_SIZE = 24;

    /** Shoe sizes (39, 42.5) or apparel sizes (S, XL). Anything else is a typo or a probe (AC 2.11). */
    private static final Pattern SIZE = Pattern.compile("\\d{2}(\\.5)?|XXS|XS|S|M|L|XL|XXL|XXXL");

    public ProductTileQuery {
        sizes = sizes == null ? List.of() : List.copyOf(sizes);
        colors = colors == null ? List.of() : List.copyOf(colors);
        sort = sort == null ? ProductSort.NEWEST : sort;

        // Names are the HTTP parameter names, because the client reads them.
        var errors = new ArrayList<InvalidParam>();
        if (minPrice != null && minPrice < 0) {
            errors.add(new InvalidParam("minPrice", "Giá tối thiểu không được âm"));
        }
        if (maxPrice != null && maxPrice < 0) {
            errors.add(new InvalidParam("maxPrice", "Giá tối đa không được âm"));
        }
        if (minPrice != null && maxPrice != null && minPrice >= 0 && maxPrice >= 0 && minPrice > maxPrice) {
            errors.add(new InvalidParam("maxPrice", "Giá tối đa phải lớn hơn hoặc bằng giá tối thiểu"));
        }
        if (page < 1) {
            errors.add(new InvalidParam("page", "Số trang bắt đầu từ 1"));
        }
        if (gender == Gender.UNISEX) {
            errors.add(new InvalidParam("gender", "Chỉ lọc được Nam hoặc Nữ"));
        }
        for (String size : sizes) {
            if (!SIZE.matcher(size).matches()) {
                errors.add(new InvalidParam("size", "Size không hợp lệ: " + size));
            }
        }
        if (!errors.isEmpty()) {
            throw new InvalidProductFilterException(errors);
        }
    }

    /** Rows to skip; long so a huge page number cannot overflow. */
    public long offset() {
        return (long) (page - 1) * PAGE_SIZE;
    }
}
