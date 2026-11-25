package com.cadence.catalog.infrastructure.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;

import org.junit.jupiter.api.Test;

class CatalogPropertiesTest {

    private static final String TEMPLATE = "https://img.example/{key}";

    @Test
    void constructor_acceptsAscendingBoundaries() {
        assertThat(new CatalogProperties(List.of(1L, 2L, 3L), TEMPLATE).priceBandBoundaries())
                .containsExactly(1L, 2L, 3L);
        assertThat(new CatalogProperties(List.of(), TEMPLATE).priceBandBoundaries()).isEmpty();
    }

    @Test
    void constructor_namesTheSettingWhenBoundariesAreWrong() {
        for (List<Long> wrong :
                List.of(List.of(2_000_000L, 1_000_000L), List.of(1L, 1L), List.of(0L, 5L))) {
            assertThatThrownBy(() -> new CatalogProperties(wrong, TEMPLATE))
                    .as(wrong.toString())
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("cadence.catalog.price-band-boundaries");
        }
    }

    @Test
    void constructor_requiresTheKeyPlaceholder() {
        assertThatThrownBy(() -> new CatalogProperties(List.of(1L), "https://img.example/a.jpg"))
                .hasMessageContaining("{key}");
    }

    @Test
    void urlOf_putsTheKeyIntoTheTemplateAndKeepsSlashes() {
        var urls = new ImageUrls(new CatalogProperties(List.of(1L), TEMPLATE + "?w=10"));

        assertThat(urls.urlOf("products/tempo 2.jpg"))
                .isEqualTo("https://img.example/products/tempo%202.jpg?w=10");
    }
}
