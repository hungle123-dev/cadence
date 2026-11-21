package com.cadence.catalog.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.cadence.catalog.application.dto.ProductSort;
import com.cadence.catalog.application.dto.ProductTileQuery;
import com.cadence.catalog.domain.exception.InvalidProductFilterException;
import com.cadence.catalog.domain.model.Gender;
import com.cadence.common.domain.error.InvalidParam;

/** AC 2.11 and A2: filter validation and paging. No Spring. */
class ProductTileQueryTest {

    @Test
    void missing_values_get_defaults() {
        var query = new ProductTileQuery(null, null, null, null, null, null, null, 1);

        assertThat(query.sizes()).isEmpty();
        assertThat(query.colors()).isEmpty();
        assertThat(query.sort()).isEqualTo(ProductSort.NEWEST);
    }

    @Test
    void negative_min_price_is_rejected() {
        assertThat(invalidNames(() -> query(-5L, null, 1, null))).containsExactly("minPrice");
    }

    @Test
    void max_below_min_is_rejected() {
        assertThat(invalidNames(() -> query(500_000L, 100_000L, 1, null))).containsExactly("maxPrice");
    }

    @Test
    void both_prices_negative_report_each_parameter_once() {
        assertThat(invalidNames(() -> query(-5L, -10L, 1, null))).containsExactlyInAnyOrder("minPrice", "maxPrice");
    }

    @Test
    void equal_min_and_max_is_accepted() {
        assertThat(query(500_000L, 500_000L, 1, null).minPrice()).isEqualTo(500_000L);
    }

    @Test
    void page_zero_is_rejected() {
        assertThat(invalidNames(() -> query(null, null, 0, null))).containsExactly("page");
    }

    @Test
    void unisex_filter_is_rejected() {
        assertThat(invalidNames(() -> query(null, null, 1, Gender.UNISEX))).containsExactly("gender");
    }

    @Test
    void every_wrong_value_is_reported_at_once() {
        assertThat(invalidNames(() -> query(-5L, null, 0, null))).containsExactlyInAnyOrder("minPrice", "page");
    }

    @Test
    void offset_follows_the_fixed_page_size() {
        assertThat(query(null, null, 1, null).offset()).isZero();
        assertThat(query(null, null, 3, null).offset()).isEqualTo(48);
    }

    @Test
    void huge_page_does_not_overflow() {
        assertThat(query(null, null, Integer.MAX_VALUE, null).offset()).isPositive();
    }

    @Test
    void changing_the_caller_list_does_not_change_the_query() {
        var sizes = new ArrayList<>(List.of("42"));
        var query = new ProductTileQuery(null, sizes, null, null, null, null, null, 1);

        sizes.add("43");

        assertThat(query.sizes()).containsExactly("42");
    }

    @Test
    void shoe_and_apparel_sizes_are_accepted() {
        var query = new ProductTileQuery(null, List.of("42", "42.5", "M", "XXL"), null, null, null, null, null, 1);

        assertThat(query.sizes()).hasSize(4);
    }

    @Test
    void unknown_size_is_rejected() {
        assertThat(invalidNames(() -> new ProductTileQuery(null, List.of("abc"), null, null, null, null, null, 1)))
                .containsExactly("size");
    }

    private static ProductTileQuery query(Long minPrice, Long maxPrice, int page, Gender gender) {
        return new ProductTileQuery(null, List.of(), List.of(), gender, minPrice, maxPrice, ProductSort.NEWEST, page);
    }

    private static List<String> invalidNames(Runnable create) {
        var error = catchThrowableOfType(InvalidProductFilterException.class, create::run);
        assertThat(error).as("expected InvalidProductFilterException").isNotNull();
        return error.invalidParams().stream().map(InvalidParam::name).toList();
    }
}
