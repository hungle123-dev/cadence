package com.cadence.catalog.application;

import static com.cadence.catalog.application.ProductFixtures.CATEGORIES;
import static com.cadence.catalog.application.ProductFixtures.NOW;
import static com.cadence.catalog.application.ProductFixtures.colorway;
import static com.cadence.catalog.application.ProductFixtures.product;
import static com.cadence.catalog.application.ProductFixtures.published;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.cadence.catalog.application.dto.ProductSnapshot;
import com.cadence.catalog.application.port.driven.ProductDetailReadPort;
import com.cadence.catalog.application.service.ProductDetailViewAssembler;
import com.cadence.catalog.application.service.ProductQueryService;
import com.cadence.catalog.domain.exception.ProductNotFoundException;
import com.cadence.catalog.domain.model.ProductPublication;
import com.cadence.catalog.domain.model.ProductStatus;

/** R-VIS and A7: only visible products come back; anything else is "not found". No Spring, no database. */
class ProductQueryServiceTest {

    @Test
    void published_product_is_returned() {
        var service = serviceWith(product("tempo-2", published(), colorway("den")), NOW);

        assertThat(service.getProductDetail("tempo-2").slug()).isEqualTo("tempo-2");
    }

    @Test
    void unknown_slug_is_not_found() {
        var service = serviceWith(product("tempo-2", published(), colorway("den")), NOW);

        assertThatThrownBy(() -> service.getProductDetail("khong-co-that"))
                .isInstanceOf(ProductNotFoundException.class);
    }

    @Test
    void draft_is_not_found() {
        var draft = new ProductPublication(ProductStatus.DRAFT, null);
        var service = serviceWith(product("tempo-2", draft, colorway("den")), NOW);

        assertThatThrownBy(() -> service.getProductDetail("tempo-2"))
                .isInstanceOf(ProductNotFoundException.class);
    }

    @Test
    void scheduled_product_is_not_found_before_publish_time() {
        var scheduled = new ProductPublication(ProductStatus.SCHEDULED, NOW);
        var service = serviceWith(product("tempo-2", scheduled, colorway("den")), NOW.minusSeconds(1));

        assertThatThrownBy(() -> service.getProductDetail("tempo-2"))
                .isInstanceOf(ProductNotFoundException.class);
    }

    @Test
    void scheduled_product_appears_exactly_at_publish_time() {
        var scheduled = new ProductPublication(ProductStatus.SCHEDULED, NOW);
        var service = serviceWith(product("tempo-2", scheduled, colorway("den")), NOW);

        assertThat(service.getProductDetail("tempo-2").slug()).isEqualTo("tempo-2");
    }

    /** Fake port holding one stored product, a real assembler, and a clock frozen at `now`. */
    private static ProductQueryService serviceWith(ProductSnapshot stored, Instant now) {
        ProductDetailReadPort products = slug -> slug.equals(stored.slug()) ? Optional.of(stored) : Optional.empty();
        return new ProductQueryService(products, new ProductDetailViewAssembler(CATEGORIES), Clock.fixed(now, ZoneOffset.UTC));
    }
}
