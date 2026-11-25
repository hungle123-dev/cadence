package com.cadence.catalog.infrastructure.restapi.uidto;

import java.util.List;

import jakarta.validation.constraints.Pattern;

import org.jspecify.annotations.Nullable;

import com.cadence.catalog.application.dto.ProductListQuery;
import com.cadence.catalog.application.dto.ProductSort;
import com.cadence.catalog.domain.model.ColorFamily;
import com.cadence.catalog.domain.model.Gender;
import com.cadence.catalog.domain.model.Sport;
import com.cadence.common.web.validation.ValidEnum;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Query parameters of the product list and its facets (FR-CAT-01…04), bound by
 * {@code @ParameterObject @Valid}. Two layers of checks:
 *
 * <ul>
 *   <li>here, the <b>type</b>: a number is a number, a colour is a known colour. A wrong one is
 *       answered by the shared handler as {@code VALIDATION_ERROR}, every bad field listed;
 *   <li>in {@link ProductListQuery} and the service, the <b>value</b>: page 0, an unknown category,
 *       a price band that is not configured. Those answer {@code CATALOG_INVALID_FILTER}.
 * </ul>
 *
 * <p>Numbers arrive as text on purpose: Spring binds a record through its constructor, and one
 * component that cannot be converted ({@code page=abc}) stops the record from being built, so the
 * other wrong fields would go unreported. As text with a {@code @Pattern}, all are reported.
 */
public record ProductListRequest(
        @Schema(
                        description =
                                "The category page being shown (/danh-muc/{slug}); omitted on the"
                                        + " all products page",
                        example = "giay-chay-bo")
                @Nullable String category,
        @Schema(description = "Repeatable category filter; not allowed together with category")
                @Nullable List<String> categories,
        @Schema(description = "Repeatable; values of one filter are OR-ed")
                @Nullable List<@ValidEnum(enumClass = Gender.class) String> gender,
        @Schema(description = "Repeatable")
                @Nullable List<@ValidEnum(enumClass = Sport.class) String> sport,
        @Schema(description = "Repeatable; a product matches when any colorway has the colour")
                @Nullable List<@ValidEnum(enumClass = ColorFamily.class) String> color,
        @Schema(description = "Repeatable; only sizes in stock match", example = "42")
                @Nullable List<String> size,
        @Schema(
                        description =
                                "Repeatable configured band, min-max or min- for the top one,"
                                        + " compared with the effective price",
                        example = "1000000-2000000")
                @Nullable List<@Pattern(regexp = PRICE_BAND, message = MUST_BE_PRICE_BAND) String>
                        price,
        @Schema(
                        description = "Only products with an active sale",
                        allowableValues = {"true", "false"})
                @Pattern(regexp = "true|false", message = "Phải là true hoặc false") @Nullable String onSale,
        @Schema(implementation = ProductSort.class, defaultValue = "FEATURED")
                @ValidEnum(enumClass = ProductSort.class)
                @Nullable String sort,
        @Schema(
                        description = "1-based; past the last page answers an empty page",
                        type = "integer",
                        format = "int32",
                        minimum = "1",
                        defaultValue = "1")
                @Pattern(regexp = PAGE_NUMBER, message = MUST_BE_WHOLE_NUMBER) @Nullable String page) {

    static final String PRICE_BAND = "\\d{1,15}-(\\d{1,15})?";

    static final String MUST_BE_PRICE_BAND = "Phải có dạng min-max hoặc min-";

    /** A sign and digits, so -5 passes here and is refused by ProductListQuery as a value. */
    static final String PAGE_NUMBER = "-?\\d{1,9}";

    static final String MUST_BE_WHOLE_NUMBER = "Phải là số nguyên";

    public ProductListRequest {
        categories = categories == null ? null : List.copyOf(categories);
        gender = gender == null ? null : List.copyOf(gender);
        sport = sport == null ? null : List.copyOf(sport);
        color = color == null ? null : List.copyOf(color);
        size = size == null ? null : List.copyOf(size);
        price = price == null ? null : List.copyOf(price);
    }

    /** Every text here already passed its check, so each conversion below succeeds. */
    public ProductListQuery toQuery() {
        return new ProductListQuery(
                isBlank(category) ? null : category,
                nonBlank(categories),
                nonBlank(gender).stream().map(Gender::valueOf).toList(),
                nonBlank(sport).stream().map(Sport::valueOf).toList(),
                nonBlank(color).stream().map(ColorFamily::valueOf).toList(),
                nonBlank(size),
                nonBlank(price),
                "true".equals(onSale),
                isBlank(sort) ? null : ProductSort.valueOf(sort),
                isBlank(page) ? 1 : Integer.parseInt(page));
    }

    private static List<String> nonBlank(@Nullable List<String> values) {
        return values == null ? List.of() : values.stream().filter(v -> !isBlank(v)).toList();
    }

    private static boolean isBlank(@Nullable String value) {
        return value == null || value.isBlank();
    }
}
