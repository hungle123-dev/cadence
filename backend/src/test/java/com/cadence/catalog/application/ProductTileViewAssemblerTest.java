package com.cadence.catalog.application;

import static com.cadence.catalog.application.ProductFixtures.NOW;
import static com.cadence.catalog.application.RecordingTileReadPort.NO_FILTERS;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.cadence.catalog.application.dto.ProductSnapshot;
import com.cadence.catalog.application.dto.ProductTileSnapshotPage;
import com.cadence.catalog.application.service.ProductTileViewAssembler;
import com.cadence.catalog.domain.model.ColorwayPricing;
import com.cadence.catalog.domain.model.Sale;

/** PLP view: price for the moment (R-PRICE) and paging (A2). No Spring, no port. */
class ProductTileViewAssemblerTest {

    private final ProductTileViewAssembler assembler = new ProductTileViewAssembler();

    // ---------- Price (R-PRICE, AC 1.5, 1.6) ----------

    @Test
    void price_is_the_sale_price_while_the_sale_runs() {
        Sale running = new Sale(990_000, NOW.minus(Duration.ofDays(1)), NOW.plus(Duration.ofDays(30)));
        var view = assembler.toView(page(1, tile(new ColorwayPricing(1_290_000, running), true)), NO_FILTERS, 1, NOW);

        var price = view.items().getFirst().price();
        assertThat(price.amount()).isEqualTo(990_000);
        assertThat(price.compareAt()).isEqualTo(1_290_000);
    }

    @Test
    void expired_sale_shows_only_the_list_price() {
        Sale ended = new Sale(990_000, NOW.minus(Duration.ofDays(30)), NOW.minus(Duration.ofDays(1)));
        var view = assembler.toView(page(1, tile(new ColorwayPricing(1_290_000, ended), true)), NO_FILTERS, 1, NOW);

        var price = view.items().getFirst().price();
        assertThat(price.amount()).isEqualTo(1_290_000);
        assertThat(price.compareAt()).isNull();
    }

    // ---------- Paging (AC 1.7, A2) ----------

    @Test
    void no_items_means_no_pages() {
        assertThat(assembler.toView(page(0), NO_FILTERS, 1, NOW).totalPages()).isZero();
    }

    @Test
    void total_pages_round_up() {
        assertThat(assembler.toView(page(24), NO_FILTERS, 1, NOW).totalPages()).isEqualTo(1);
        assertThat(assembler.toView(page(25), NO_FILTERS, 1, NOW).totalPages()).isEqualTo(2);
        assertThat(assembler.toView(page(30), NO_FILTERS, 1, NOW).totalPages()).isEqualTo(2);
    }

    @Test
    void page_numbers_and_filters_are_passed_through() {
        var view = assembler.toView(page(30), NO_FILTERS, 2, NOW);

        assertThat(view.page()).isEqualTo(2);
        assertThat(view.pageSize()).isEqualTo(24);
        assertThat(view.totalItems()).isEqualTo(30);
        assertThat(view.filters()).isSameAs(NO_FILTERS);
    }

    // ---------- Tile fields (AC 1.1, 1.11) ----------

    @Test
    void out_of_stock_tile_without_image_is_kept() {
        var view = assembler.toView(page(1, tile(new ColorwayPricing(1_290_000, null), false)), NO_FILTERS, 1, NOW);

        var tile = view.items().getFirst();
        assertThat(tile.inStock()).isFalse();
        assertThat(tile.image()).isNull();
        assertThat(tile.colorCount()).isEqualTo(3);
        assertThat(tile.productSlug()).isEqualTo("tempo-2");
        assertThat(tile.colorwaySlug()).isEqualTo("den");
    }

    private static ProductTileSnapshotPage page(long totalItems, ProductTileSnapshotPage.Tile... tiles) {
        return new ProductTileSnapshotPage(List.of(tiles), totalItems);
    }

    private static ProductTileSnapshotPage.Tile tile(ColorwayPricing pricing, boolean inStock) {
        ProductSnapshot.Image noImage = null;
        return new ProductTileSnapshotPage.Tile("tempo-2", "den", "Cadence Tempo 2", "Đen", noImage, pricing, 3, inStock);
    }
}
