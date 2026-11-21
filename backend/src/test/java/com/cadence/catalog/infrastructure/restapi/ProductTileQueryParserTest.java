package com.cadence.catalog.infrastructure.restapi;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.cadence.catalog.application.dto.ProductSort;
import com.cadence.catalog.domain.exception.InvalidProductFilterException;
import com.cadence.catalog.domain.model.ColorFamily;
import com.cadence.common.domain.error.InvalidParam;

/** AC 2.11: raw text in, typed query out, or every bad parameter listed. No Spring. */
class ProductTileQueryParserTest {

    @Test
    void nothing_given_means_defaults() {
        var query = ProductTileQueryParser.toQuery(null, null, null, null, null, null, null, null);

        assertThat(query.sort()).isEqualTo(ProductSort.NEWEST);
        assertThat(query.page()).isEqualTo(1);
        assertThat(query.colors()).isEmpty();
    }

    @Test
    void valid_text_becomes_typed_values() {
        var query = ProductTileQueryParser.toQuery("giay", List.of("42"), List.of("BLACK", "WHITE"), "MEN",
                "500000", "1000000", "PRICE_ASC", "2");

        assertThat(query.colors()).containsExactly(ColorFamily.BLACK, ColorFamily.WHITE);
        assertThat(query.minPrice()).isEqualTo(500_000L);
        assertThat(query.sort()).isEqualTo(ProductSort.PRICE_ASC);
        assertThat(query.page()).isEqualTo(2);
    }

    @Test
    void blank_values_count_as_missing() {
        var query = ProductTileQueryParser.toQuery("", null, null, "", "", "", "", "");

        assertThat(query.categorySlug()).isNull();
        assertThat(query.minPrice()).isNull();
        assertThat(query.page()).isEqualTo(1);
    }

    @Test
    void every_bad_type_is_reported_at_once() {
        assertThat(invalidNames(() -> ProductTileQueryParser.toQuery(null, null, List.of("XYZ"), "ALIEN",
                "1.5tr", "abc", "cheapest", "x")))
                .containsExactlyInAnyOrder("color", "gender", "minPrice", "maxPrice", "sort", "page");
    }

    @Test
    void values_are_still_checked_by_the_query() {
        assertThat(invalidNames(() -> ProductTileQueryParser.toQuery(null, null, null, null, "-5", null, null, null)))
                .containsExactly("minPrice");
    }

    private static List<String> invalidNames(Runnable call) {
        var error = catchThrowableOfType(InvalidProductFilterException.class, call::run);
        assertThat(error).as("expected InvalidProductFilterException").isNotNull();
        return error.invalidParams().stream().map(InvalidParam::name).toList();
    }
}
