package com.cadence.catalog.application;

import static com.cadence.catalog.application.ProductFixtures.CATEGORIES;
import static com.cadence.catalog.application.ProductFixtures.NOW;
import static com.cadence.catalog.application.ProductFixtures.colorway;
import static com.cadence.catalog.application.ProductFixtures.product;
import static com.cadence.catalog.application.ProductFixtures.published;
import static com.cadence.catalog.application.ProductFixtures.size;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;

import org.junit.jupiter.api.Test;

import com.cadence.catalog.application.dto.ProductDetailView;
import com.cadence.catalog.application.service.ProductDetailViewAssembler;
import com.cadence.catalog.domain.model.ColorwayPricing;
import com.cadence.catalog.domain.model.Sale;

/** PDP view: price for the moment (R-PRICE), stock per colour (R-STOCK), breadcrumb. No Spring. */
class ProductDetailViewAssemblerTest {

    private final ProductDetailViewAssembler assembler = new ProductDetailViewAssembler(CATEGORIES);

    // ---------- Price (R-PRICE, AC 1.5, 1.6) ----------

    @Test
    void price_is_the_sale_price_while_the_sale_runs() {
        Sale running = new Sale(1_990_000, NOW.minus(Duration.ofDays(1)), NOW.plus(Duration.ofDays(30)));
        var view = assembler.toView(
                product("tempo-2", published(), colorway("den", new ColorwayPricing(2_490_000, running), size("40", true))),
                NOW);

        var price = view.colorways().getFirst().price();
        assertThat(price.amount()).isEqualTo(1_990_000);
        assertThat(price.compareAt()).isEqualTo(2_490_000L);
    }

    @Test
    void price_is_the_list_price_outside_the_sale() {
        Sale ended = new Sale(1_990_000, NOW.minus(Duration.ofDays(30)), NOW.minus(Duration.ofDays(1)));
        var view = assembler.toView(
                product("tempo-2", published(), colorway("den", new ColorwayPricing(2_490_000, ended), size("40", true))),
                NOW);

        var price = view.colorways().getFirst().price();
        assertThat(price.amount()).isEqualTo(2_490_000);
        assertThat(price.compareAt()).isNull();
    }

    // ---------- Stock (R-STOCK, AC 1.11, 3.4) ----------

    @Test
    void colorway_is_out_of_stock_when_every_size_is() {
        var view = assembler.toView(
                product("tempo-2", published(),
                        colorway("den", new ColorwayPricing(2_490_000, null), size("40", false), size("41", false))),
                NOW);

        assertThat(view.colorways().getFirst().inStock()).isFalse();
    }

    @Test
    void colorway_is_in_stock_when_one_size_is() {
        var view = assembler.toView(
                product("tempo-2", published(),
                        colorway("den", new ColorwayPricing(2_490_000, null), size("40", false), size("41", true))),
                NOW);

        assertThat(view.colorways().getFirst().inStock()).isTrue();
    }

    // ---------- Shape ----------

    @Test
    void breadcrumb_starts_at_the_root() {
        var view = assembler.toView(product("tempo-2", published(), colorway("den")), NOW);

        assertThat(view.breadcrumb())
                .extracting(ProductDetailView.Breadcrumb::slug)
                .containsExactly("giay", "giay-chay-bo");
    }

    @Test
    void colorways_keep_the_order_of_the_snapshot() {
        var view = assembler.toView(
                product("tempo-2", published(), colorway("den"), colorway("trang"), colorway("xanh")), NOW);

        assertThat(view.colorways())
                .extracting(ProductDetailView.Colorway::slug)
                .containsExactly("den", "trang", "xanh");
    }
}
