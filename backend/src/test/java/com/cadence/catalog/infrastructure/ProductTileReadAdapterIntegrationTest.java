package com.cadence.catalog.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import com.cadence.TestcontainersConfiguration;
import com.cadence.catalog.application.dto.ProductSort;
import com.cadence.catalog.application.dto.ProductTilePageView;
import com.cadence.catalog.application.dto.ProductTileQuery;
import com.cadence.catalog.application.dto.ProductTileSnapshotPage;
import com.cadence.catalog.application.port.driven.ProductTileReadPort;
import com.cadence.catalog.domain.model.ColorFamily;
import com.cadence.catalog.domain.model.Gender;

/**
 * ProductTileReadAdapter on a real PostgreSQL 18 with sql/plp-tiles.sql: the SQL copies of R-VIS, R-PRICE and
 * R-STOCK must agree with the domain. @Transactional rolls the data back after each test.
 */
@SpringBootTest
@Import(TestcontainersConfiguration.class)
@Transactional
@Sql({ "classpath:db/seed/R__seed_categories.sql", "classpath:sql/plp-tiles.sql" })
class ProductTileReadAdapterIntegrationTest {

    private static final Instant NOW = Instant.parse("2026-12-04T12:00:00Z");

    private static final UUID GIAY_CHAY_BO = UUID.fromString("00000000-0000-7000-8000-000000000011");
    private static final UUID GIAY_LIFESTYLE = UUID.fromString("00000000-0000-7000-8000-000000000012");
    private static final UUID AO_THUN = UUID.fromString("00000000-0000-7000-8000-000000000021");
    private static final UUID AO_CHAY_BO = UUID.fromString("00000000-0000-7000-8000-000000000022"); // no products
    private static final Set<UUID> SHOES = Set.of(GIAY_CHAY_BO, GIAY_LIFESTYLE);

    @Autowired
    ProductTileReadPort port;

    // ---------- Tiles (AC 1.1, 1.11) ----------

    @Test
    void every_colorway_is_its_own_tile() {
        var result = search(Set.of(GIAY_CHAY_BO), new Filter());

        assertThat(keys(result)).contains("run-a/den", "run-a/trang", "run-a/do");
        assertThat(tile(result, "run-a/den").colorCount()).isEqualTo(3);
    }

    @Test
    void tile_shows_the_first_image_by_position() {
        var image = tile(search(SHOES, new Filter()), "run-a/den").image();

        assertThat(image).isNotNull();
        assertThat(image.storageKey()).isEqualTo("test/run-a-den-1.jpg");
    }

    @Test
    void sold_out_colorway_without_image_is_still_a_tile() {
        var doTile = tile(search(SHOES, new Filter()), "run-a/do");

        assertThat(doTile.inStock()).isFalse();
        assertThat(doTile.image()).isNull();
        assertThat(tile(search(SHOES, new Filter()), "run-a/den").inStock()).isTrue();
    }

    @Test
    void pricing_comes_back_raw() {
        var pricing = tile(search(SHOES, new Filter()), "run-a/den").pricing();

        assertThat(pricing.listPrice()).isEqualTo(1_500_000);
        assertThat(pricing.sale()).isNotNull();
        assertThat(pricing.sale().price()).isEqualTo(900_000);
    }

    // ---------- Visibility (R-VIS, AC 1.3, 1.4) and categories (AC 1.2) ----------

    @Test
    void hidden_products_never_show_even_in_stock() {
        assertThat(productSlugs(search(SHOES, new Filter()))).doesNotContain("draft-x", "sched-future");
    }

    @Test
    void scheduled_product_shows_once_its_time_has_passed() {
        assertThat(productSlugs(search(SHOES, new Filter()))).contains("sched-past");
    }

    @Test
    void only_the_given_categories_are_searched() {
        assertThat(productSlugs(search(Set.of(GIAY_LIFESTYLE), new Filter()))).containsOnly("run-c");
    }

    // ---------- Paging (AC 1.7, 1.8, A2) ----------

    @Test
    void thirty_tiles_split_into_24_and_6() {
        var page1 = search(Set.of(AO_THUN), new Filter().page(1));
        var page2 = search(Set.of(AO_THUN), new Filter().page(2));
        var page3 = search(Set.of(AO_THUN), new Filter().page(3));

        assertThat(page1.tiles()).hasSize(24);
        assertThat(page2.tiles()).hasSize(6);
        assertThat(page3.tiles()).isEmpty();
        assertThat(List.of(page1.totalItems(), page2.totalItems(), page3.totalItems())).containsOnly(30L);
    }

    @Test
    void equal_prices_never_repeat_or_lose_a_tile_across_pages() {
        for (ProductSort sort : ProductSort.values()) {
            Set<String> seen = new HashSet<>();
            seen.addAll(keys(search(Set.of(AO_THUN), new Filter().sort(sort).page(1))));
            seen.addAll(keys(search(Set.of(AO_THUN), new Filter().sort(sort).page(2))));

            assertThat(seen).as("sort " + sort).hasSize(30);
        }
    }

    // ---------- Filters (AC 2.1–2.5) ----------

    @Test
    void size_filter_keeps_only_colorways_with_that_size_in_stock() {
        var result = search(SHOES, new Filter().sizes("42"));

        assertThat(keys(result)).containsExactlyInAnyOrder("run-a/den", "run-c/xanh");
    }

    @Test
    void colours_of_one_filter_are_ored() {
        var result = search(SHOES, new Filter().colors(ColorFamily.BLACK, ColorFamily.WHITE));

        assertThat(keys(result)).containsExactlyInAnyOrder("run-a/den", "run-a/trang", "run-b/den");
    }

    @Test
    void different_filters_are_anded() {
        var result = search(SHOES, new Filter().colors(ColorFamily.BLACK).sizes("42"));

        assertThat(keys(result)).containsExactly("run-a/den");
    }

    @Test
    void price_filter_uses_the_effective_price() {
        var result = search(SHOES, new Filter().price(500_000L, 1_000_000L));

        // run-a/den: 1.5M list but 900k on sale; run-c/xanh: its 500k sale ended, so 800k
        assertThat(keys(result)).containsExactlyInAnyOrder("run-a/den", "run-a/trang", "run-c/xanh");
    }

    @Test
    void gender_filter_includes_unisex() {
        var result = search(SHOES, new Filter().gender(Gender.MEN));

        assertThat(productSlugs(result)).containsOnly("run-a", "run-c");
    }

    // ---------- Sort (AC 2.6, 2.7) ----------

    @Test
    void newest_first_by_default_and_colours_in_position_order() {
        var result = search(SHOES, new Filter());

        assertThat(keys(result)).containsExactly(
                "sched-past/xanh-la", "run-b/den", "run-c/xanh", "run-a/den", "run-a/trang", "run-a/do");
    }

    @Test
    void price_ascending_uses_the_effective_price() {
        var result = search(SHOES, new Filter().sort(ProductSort.PRICE_ASC));

        assertThat(keys(result)).containsExactly(
                "run-c/xanh", "run-a/den", "run-a/trang", "sched-past/xanh-la", "run-b/den", "run-a/do");
    }

    @Test
    void price_descending_uses_the_effective_price() {
        var result = search(SHOES, new Filter().sort(ProductSort.PRICE_DESC));

        assertThat(keys(result)).first().isEqualTo("run-a/do");
        assertThat(keys(result)).last().isEqualTo("run-c/xanh");
    }

    // ---------- Filter options (A1, AC 2.12) ----------

    @Test
    void sizes_are_in_size_order_and_in_stock_only() {
        // 43 exists on run-a but is sold out (den) or has no flag (do); 44 belongs to a draft
        assertThat(port.findFilterOptions(SHOES, NOW).sizes()).containsExactly("40", "41", "42");
    }

    @Test
    void colours_come_in_enum_order() {
        assertThat(port.findFilterOptions(SHOES, NOW).colors()).containsExactly(
                ColorFamily.BLACK, ColorFamily.WHITE, ColorFamily.RED, ColorFamily.GREEN, ColorFamily.BLUE);
    }

    @Test
    void options_only_cover_the_given_categories() {
        var options = port.findFilterOptions(Set.of(GIAY_LIFESTYLE), NOW);

        assertThat(options.colors()).containsExactly(ColorFamily.BLUE);
        assertThat(options.sizes()).containsExactly("42");
    }

    @Test
    void unisex_products_offer_both_genders() {
        assertThat(port.findFilterOptions(Set.of(GIAY_LIFESTYLE), NOW).genders())
                .containsExactly(Gender.MEN, Gender.WOMEN);
    }

    @Test
    void price_range_uses_the_effective_price() {
        // run-c's 500k sale has ended, so the lowest price is its 800k list price
        var range = port.findFilterOptions(SHOES, NOW).priceRange();

        assertThat(range.min()).isEqualTo(800_000);
        assertThat(range.max()).isEqualTo(2_000_000);
    }

    @Test
    void empty_category_has_no_options() {
        var options = port.findFilterOptions(Set.of(AO_CHAY_BO), NOW);

        assertThat(options.sizes()).isEmpty();
        assertThat(options.colors()).isEmpty();
        assertThat(options.genders()).isEmpty();
        assertThat(options.priceRange()).isEqualTo(new ProductTilePageView.PriceRange(0, 0));
    }

    // ---------- Helpers ----------

    private ProductTileSnapshotPage search(Set<UUID> categoryIds, Filter filter) {
        return port.findTiles(filter.toQuery(), categoryIds, NOW);
    }

    private static List<String> keys(ProductTileSnapshotPage result) {
        return result.tiles().stream().map(tile -> tile.productSlug() + "/" + tile.colorwaySlug()).toList();
    }

    private static Set<String> productSlugs(ProductTileSnapshotPage result) {
        return new HashSet<>(result.tiles().stream().map(ProductTileSnapshotPage.Tile::productSlug).toList());
    }

    private static ProductTileSnapshotPage.Tile tile(ProductTileSnapshotPage result, String key) {
        return result.tiles().stream()
                .filter(tile -> (tile.productSlug() + "/" + tile.colorwaySlug()).equals(key))
                .findFirst()
                .orElseThrow(() -> new AssertionError("no tile " + key + " in " + keys(result)));
    }

    /** Builds a ProductTileQuery; each test sets only the filters it cares about. */
    private static final class Filter {
        private List<String> sizes = List.of();
        private List<ColorFamily> colors = List.of();
        private Gender gender;
        private Long minPrice;
        private Long maxPrice;
        private ProductSort sort = ProductSort.NEWEST;
        private int page = 1;

        Filter sizes(String... values) { sizes = List.of(values); return this; }
        Filter colors(ColorFamily... values) { colors = List.of(values); return this; }
        Filter gender(Gender value) { gender = value; return this; }
        Filter price(Long min, Long max) { minPrice = min; maxPrice = max; return this; }
        Filter sort(ProductSort value) { sort = value; return this; }
        Filter page(int value) { page = value; return this; }

        ProductTileQuery toQuery() {
            return new ProductTileQuery(null, sizes, colors, gender, minPrice, maxPrice, sort, page);
        }
    }
}
