package com.cadence.catalog.infrastructure.restapi;

import java.util.ArrayList;
import java.util.List;

import org.jspecify.annotations.Nullable;

import com.cadence.catalog.application.dto.ProductSort;
import com.cadence.catalog.application.dto.ProductTileQuery;
import com.cadence.catalog.domain.exception.InvalidProductFilterException;
import com.cadence.catalog.domain.model.ColorFamily;
import com.cadence.catalog.domain.model.Gender;
import com.cadence.common.domain.error.InvalidParam;

/**
 * Turns raw PLP query parameters into a ProductTileQuery. Parameters arrive as text so that a wrong value
 * becomes CATALOG_INVALID_FILTER with every bad parameter listed (AC 2.11), instead of Spring's generic 400.
 * This class checks types only (is it a number, a known colour?); ProductTileQuery checks the values.
 */
final class ProductTileQueryParser {

    private ProductTileQueryParser() {
    }

    static ProductTileQuery toQuery(@Nullable String category, @Nullable List<String> size,
            @Nullable List<String> color, @Nullable String gender, @Nullable String minPrice,
            @Nullable String maxPrice, @Nullable String sort, @Nullable String page) {
        List<InvalidParam> errors = new ArrayList<>();

        List<ColorFamily> colors = new ArrayList<>();
        for (String value : orEmpty(color)) {
            ColorFamily parsed = toEnum(ColorFamily.class, value, "color", "Màu không hợp lệ: ", errors);
            if (parsed != null) {
                colors.add(parsed);
            }
        }
        Gender genderValue = toEnum(Gender.class, gender, "gender", "Giới tính không hợp lệ: ", errors);
        ProductSort sortValue = toEnum(ProductSort.class, sort, "sort", "Cách sắp xếp không hợp lệ: ", errors);
        Long min = toLong(minPrice, "minPrice", errors);
        Long max = toLong(maxPrice, "maxPrice", errors);
        Integer pageValue = toInt(page, "page", errors);

        if (!errors.isEmpty()) {
            throw new InvalidProductFilterException(errors);
        }
        // Types are fine; ProductTileQuery now checks the values (negative price, min > max, page 0, size format)
        return new ProductTileQuery(blankToNull(category), orEmpty(size), colors, genderValue, min, max, sortValue,
                pageValue == null ? 1 : pageValue);
    }

    private static <E extends Enum<E>> @Nullable E toEnum(Class<E> type, @Nullable String value, String name,
            String reason, List<InvalidParam> errors) {
        if (isBlank(value)) {
            return null;
        }
        try {
            return Enum.valueOf(type, value);
        } catch (IllegalArgumentException e) {
            errors.add(new InvalidParam(name, reason + value));
            return null;
        }
    }

    private static @Nullable Long toLong(@Nullable String value, String name, List<InvalidParam> errors) {
        if (isBlank(value)) {
            return null;
        }
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            errors.add(new InvalidParam(name, "Phải là số nguyên VND: " + value));
            return null;
        }
    }

    private static @Nullable Integer toInt(@Nullable String value, String name, List<InvalidParam> errors) {
        if (isBlank(value)) {
            return null;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            errors.add(new InvalidParam(name, "Phải là số nguyên: " + value));
            return null;
        }
    }

    private static List<String> orEmpty(@Nullable List<String> values) {
        return values == null ? List.of() : values;
    }

    private static @Nullable String blankToNull(@Nullable String value) {
        return isBlank(value) ? null : value;
    }

    private static boolean isBlank(@Nullable String value) {
        return value == null || value.isBlank();
    }
}
