package com.cadence.catalog.application.dto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.catchThrowableOfType;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.cadence.catalog.domain.exception.InvalidProductFilterException;
import com.cadence.common.domain.error.InvalidParam;

class ProductListQueryTest {

    @Test
    @DisplayName("FR-CAT-03 Không chọn cách sắp xếp thì mặc định là nổi bật")
    void of_defaultsToFeatured() {
        assertThat(query(null, List.of(), List.of(), null, 1).sort())
                .isEqualTo(ProductSort.FEATURED);
        assertThat(query(null, List.of(), List.of(), ProductSort.NEWEST, 1).sort())
                .isEqualTo(ProductSort.NEWEST);
    }

    @Test
    @DisplayName("FR-CAT-02 Mọi giá trị sai được báo cùng lúc")
    void constructor_reportsEveryWrongValueAtOnce() {
        InvalidProductFilterException ex =
                catchThrowableOfType(
                        InvalidProductFilterException.class,
                        () ->
                                query(
                                        "giay-chay-bo",
                                        List.of("giay-trail"),
                                        List.of("abc", "2000000-1000000"),
                                        null,
                                        0));

        assertThat(ex.getInvalidParams())
                .extracting(InvalidParam::name)
                .containsExactly("page", "categories", "price", "price");
    }

    @Test
    void constructor_keepsEachFilterValueOnceInTheOrderSent() {
        ProductListQuery query =
                query(null, List.of(), List.of("3000000-", "0-1000000", "3000000-"), null, 1);

        assertThat(query.prices()).containsExactly("3000000-", "0-1000000");
    }

    @Test
    void constructor_rejectsAnUnknownSizeFormat() {
        assertThatThrownBy(() ->
                        new ProductListQuery(
                                null,
                                List.of(),
                                List.of(),
                                List.of(),
                                List.of(),
                                List.of("42", "M", "42.5", "XXXXL"),
                                List.of(),
                                false,
                                null,
                                1))
                .isInstanceOf(InvalidProductFilterException.class)
                .hasFieldOrPropertyWithValue(
                        "invalidParams",
                        List.of(new InvalidParam("size", "Size không hợp lệ: XXXXL")));
    }

    @Test
    void offsetAndPriceBands_followThePageAndTheBandTexts() {
        ProductListQuery query =
                query(null, List.of(), List.of("1000000-2000000", "3000000-"), null, 3);

        assertThat(query.offset()).isEqualTo(24);
        assertThat(query.priceBands())
                .containsExactly(
                        new PriceBand(1_000_000, 2_000_000L), new PriceBand(3_000_000, null));
    }

    @Test
    @DisplayName("D-31 Bốn mức giá mặc định từ ba mốc")
    void priceBands_areBuiltFromTheBoundaries() {
        PriceBands bands = PriceBands.fromBoundaries(List.of(1_000_000L, 2_000_000L, 3_000_000L));

        assertThat(bands.bands())
                .extracting(PriceBand::toString)
                .containsExactly("0-1000000", "1000000-2000000", "2000000-3000000", "3000000-");
        assertThat(bands.contains(new PriceBand(1_000_000, 2_000_000L))).isTrue();
        assertThat(bands.contains(new PriceBand(1_000_000, 1_500_000L))).isFalse();
        assertThat(new PriceBand(1_000_000, 2_000_000L).contains(2_000_000)).isFalse();
        assertThat(new PriceBand(3_000_000, null).contains(9_000_000)).isTrue();
    }

    private static ProductListQuery query(
            String category,
            List<String> categories,
            List<String> prices,
            ProductSort sort,
            int page) {
        return new ProductListQuery(
                category,
                categories,
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                prices,
                false,
                sort,
                page);
    }
}
