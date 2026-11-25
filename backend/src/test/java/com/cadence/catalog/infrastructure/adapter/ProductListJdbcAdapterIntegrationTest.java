package com.cadence.catalog.infrastructure.adapter;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.simple.JdbcClient;

import com.cadence.catalog.CatalogIT;
import com.cadence.catalog.application.dto.PriceBand;
import com.cadence.catalog.application.dto.PriceBands;
import com.cadence.catalog.application.dto.ProductFacetsView;
import com.cadence.catalog.application.dto.ProductFacetsView.FacetCount;
import com.cadence.catalog.application.dto.ProductListQuery;
import com.cadence.catalog.application.dto.ProductSort;
import com.cadence.catalog.application.dto.ProductTileView;
import com.cadence.catalog.domain.model.ColorFamily;
import com.cadence.catalog.domain.model.Gender;
import com.cadence.catalog.domain.model.Sport;
import com.cadence.common.application.PageResult;

class ProductListJdbcAdapterIntegrationTest extends CatalogIT {

    /** 42 sample products plus flash-runner; future-runner is coming soon, two are hidden. */
    private static final int ON_SALE_COUNT = 43;

    @Autowired private ProductListJdbcAdapter adapter;

    @Autowired private PriceBands priceBands;

    @Autowired private JdbcClient jdbc;

    @Test
    @DisplayName("TC-CAT-01 12 sản phẩm mỗi trang; Xem thêm tới hết không trùng, không sót")
    void findTiles_pagesThroughEveryProductOnce() {
        for (ProductSort sort :
                List.of(ProductSort.FEATURED, ProductSort.NEWEST, ProductSort.PRICE_ASC)) {
            List<String> slugs =
                    allTiles(builder().sort(sort), NOW).stream()
                            .map(ProductTileView::slug)
                            .toList();

            assertThat(slugs).hasSize(ON_SALE_COUNT).doesNotHaveDuplicates();
        }
        PageResult<ProductTileView> first = adapter.findTiles(builder().build(), NOW);
        assertThat(first.content()).hasSize(12);
        assertThat(first.totalElements()).isEqualTo(ON_SALE_COUNT);
        assertThat(first.totalPages()).isEqualTo(4);
    }

    @Test
    @DisplayName("TC-CAT-06 Sắp mở bán, nháp, ngừng bán không có trong danh sách; tới giờ thì có")
    void findTiles_listsOnlyProductsOnSale() {
        Set<String> now = slugs(allTiles(builder(), NOW));
        Set<String> afterLaunch = slugs(allTiles(builder(), Instant.parse("2026-11-25T13:00:00Z")));

        assertThat(now).doesNotContain("future-runner", "draft-runner", "archived-runner");
        assertThat(afterLaunch)
                .contains("future-runner")
                .doesNotContain("draft-runner", "archived-runner");
        assertThat(slugs(allTiles(builder(), Instant.parse("2026-11-25T12:59:59Z"))))
                .doesNotContain("future-runner");
    }

    @Test
    @DisplayName("TC-CAT-04 Nổi bật: thứ hạng tăng dần; giá: theo giá đang bán của domain")
    void findTiles_sortsByRankAndByEffectivePrice() {
        List<String> featured =
                adapter.findTiles(builder().build(), NOW).content().stream()
                        .map(ProductTileView::slug)
                        .toList();
        List<Long> ascending =
                allTiles(builder().sort(ProductSort.PRICE_ASC), NOW).stream()
                        .map(t -> t.price().amount())
                        .toList();
        List<Long> descending =
                allTiles(builder().sort(ProductSort.PRICE_DESC), NOW).stream()
                        .map(t -> t.price().amount())
                        .toList();

        assertThat(featured).startsWith("tempo-2", "daily-run-tee", "court-classic");
        assertThat(ascending).isSorted();
        assertThat(descending.reversed()).isSorted();
    }

    @Test
    @DisplayName(
            "TC-CAT-03 Lọc size chỉ tính size còn hàng; hết hàng thì biến mất, có lại thì hiện")
    void findTiles_filtersBySizesInStockOnly() {
        assertThat(slugs(allTiles(builder().sizes("42"), NOW)))
                .contains("court-classic", "everyday-trainer")
                .doesNotContain("tempo-2", "heritage-low"); // size 42 sold out

        setAvailable("COURTCLASSIC-%-42", false);
        assertThat(slugs(allTiles(builder().sizes("42"), NOW))).doesNotContain("court-classic");
        assertThat(sizeCount(builder(), "42"))
                .isEqualTo(allTiles(builder().sizes("42"), NOW).size());

        setAvailable("COURTCLASSIC-%-42", true);
        assertThat(slugs(allTiles(builder().sizes("42"), NOW))).contains("court-classic");
    }

    @Test
    @DisplayName("TC-CAT-02 Màu và size cùng chọn: size phải còn hàng ở phối màu có màu đó")
    void findTiles_matchesSizeWithinTheChosenColour() {
        setAvailable(
                "COURTCLASSIC-01-42", false); // white colorway sold out in 42; black still has it

        assertThat(slugs(allTiles(builder().colors(ColorFamily.WHITE).sizes("42"), NOW)))
                .doesNotContain("court-classic");
        assertThat(slugs(allTiles(builder().colors(ColorFamily.BLACK).sizes("42"), NOW)))
                .contains("court-classic");
    }

    @Test
    @DisplayName("TC-CAT-02 Mỗi số đếm bằng số sản phẩm khi chọn đúng giá trị đó")
    void findFacets_countWhatChoosingEachValueWouldShow() {
        Builder chosen = builder().genders(Gender.WOMEN).colors(ColorFamily.WHITE);
        ProductFacetsView facets = adapter.findFacets(chosen.build(), priceBands, NOW);

        // Each group ignores its own choice: MEN is still counted while WOMEN is chosen.
        for (FacetCount gender : facets.genders()) {
            assertThat(gender.count())
                    .as(gender.value())
                    .isEqualTo(
                            count(
                                    builder()
                                            .colors(ColorFamily.WHITE)
                                            .genders(Gender.valueOf(gender.value()))));
        }
        for (FacetCount color : facets.colors()) {
            assertThat(color.count())
                    .as(color.value())
                    .isEqualTo(
                            count(
                                    builder()
                                            .genders(Gender.WOMEN)
                                            .colors(ColorFamily.valueOf(color.value()))));
        }
        for (FacetCount sport : facets.sports()) {
            assertThat(sport.count())
                    .isEqualTo(count(chosen.copy().sports(Sport.valueOf(sport.value()))));
        }
        for (FacetCount size : facets.sizes()) {
            assertThat(size.count())
                    .as(size.value())
                    .isEqualTo(count(chosen.copy().sizes(size.value())));
        }
        for (FacetCount price : facets.prices()) {
            assertThat(price.count())
                    .as(price.value())
                    .isEqualTo(count(chosen.copy().prices(price.value())));
        }
        // Not on a category page, so the category group is present.
        for (FacetCount category : Objects.requireNonNull(facets.categories())) {
            assertThat(category.label()).isNotBlank();
            assertThat(category.count())
                    .isEqualTo(count(chosen.copy().categories(category.value())));
        }
        assertThat(facets.onSale()).isEqualTo(count(chosen.copy().onSale()));
        assertThat(facets.genders())
                .extracting(FacetCount::value)
                .containsSubsequence("MEN", "WOMEN");
    }

    @Test
    @DisplayName("FR-CAT-02 Trang danh mục không có facet danh mục; mọi mức giá đều có mặt")
    void findFacets_leavesOutCategoriesOnACategoryPage() {
        ProductFacetsView facets =
                adapter.findFacets(builder().category("giay-chay-bo").build(), priceBands, NOW);

        assertThat(facets.categories()).isNull();
        assertThat(facets.prices())
                .extracting(FacetCount::value)
                .containsExactly("0-1000000", "1000000-2000000", "2000000-3000000", "3000000-");
        assertThat(facets.prices().stream().mapToLong(FacetCount::count).sum())
                .isEqualTo(count(builder().category("giay-chay-bo")));
    }

    @Test
    @DisplayName(
            "TC-CAT-12, TC-CAT-13 Giá lọc, giá sắp xếp bằng SQL khớp giá domain ở nhiều mốc thời"
                    + " gian")
    void sqlPriceRules_agreeWithTheDomainAtEveryMoment() {
        List<Instant> moments =
                List.of(
                        Instant.parse("2026-10-31T23:59:59Z"), // before the sample sales start
                        NOW,
                        Instant.parse("2026-11-25T23:59:59Z"), // last second of flash-runner's sale
                        Instant.parse("2026-11-26T00:00:00Z")); // its end, excluded
        for (Instant at : moments) {
            List<ProductTileView> all = allTiles(builder(), at);
            for (PriceBand band : priceBands.bands()) {
                Set<String> expected =
                        all.stream()
                                .filter(tile -> band.contains(tile.price().amount()))
                                .map(ProductTileView::slug)
                                .collect(Collectors.toSet());
                assertThat(slugs(allTiles(builder().prices(band.toString()), at)))
                        .as(band + " at " + at)
                        .isEqualTo(expected);
            }
            Set<String> onSale =
                    all.stream()
                            .filter(tile -> tile.price().onSale())
                            .map(ProductTileView::slug)
                            .collect(Collectors.toSet());
            assertThat(slugs(allTiles(builder().onSale(), at)))
                    .as("on sale at " + at)
                    .isEqualTo(onSale);
        }
        assertThat(priceOf("flash-runner", NOW)).isEqualTo(1_500_000);
        assertThat(priceOf("flash-runner", Instant.parse("2026-11-26T00:00:00Z")))
                .isEqualTo(2_000_000);
        assertThat(priceOf("mudline-trail", NOW)).isEqualTo(2_290_000);
    }

    @Test
    @DisplayName("TC-CAT-16 Ô sản phẩm: ảnh đầu tiên, số màu, nhãn Mới và Hết hàng")
    void findTiles_buildsEachTileFromTheDomainRules() {
        ProductTileView tempo = tile("tempo-2", NOW);

        assertThat(tempo.colorCount()).isEqualTo(3);
        assertThat(tempo.isNew()).isTrue();
        assertThat(tempo.inStock()).isTrue();
        assertThat(tempo.image()).isNotNull();
        assertThat(tempo.image().imageKey()).isEqualTo("photo-1631542156377-473540702434");
        assertThat(tile("court-classic", NOW).isNew()).isFalse();

        jdbc.sql("UPDATE catalog.variants SET available = false WHERE sku LIKE 'RUNCAP-%'")
                .update();
        assertThat(tile("run-cap", NOW).inStock()).isFalse();
    }

    // ---------- helpers ----------

    private List<ProductTileView> allTiles(Builder builder, Instant at) {
        List<ProductTileView> tiles = new ArrayList<>();
        for (int page = 1; ; page++) {
            PageResult<ProductTileView> result = adapter.findTiles(builder.page(page).build(), at);
            tiles.addAll(result.content());
            if (page >= result.totalPages()) {
                return tiles;
            }
        }
    }

    private long count(Builder builder) {
        return adapter.findTiles(builder.build(), NOW).totalElements();
    }

    private long sizeCount(Builder builder, String size) {
        return adapter.findFacets(builder.build(), priceBands, NOW).sizes().stream()
                .filter(facet -> facet.value().equals(size))
                .mapToLong(FacetCount::count)
                .findFirst()
                .orElse(0);
    }

    private ProductTileView tile(String slug, Instant at) {
        return allTiles(builder(), at).stream()
                .filter(t -> t.slug().equals(slug))
                .findFirst()
                .orElseThrow();
    }

    private long priceOf(String slug, Instant at) {
        return tile(slug, at).price().amount();
    }

    private void setAvailable(String skuPattern, boolean available) {
        jdbc.sql("UPDATE catalog.variants SET available = :available WHERE sku LIKE :sku")
                .param("available", available)
                .param("sku", skuPattern)
                .update();
    }

    private static Set<String> slugs(List<ProductTileView> tiles) {
        return tiles.stream().map(ProductTileView::slug).collect(Collectors.toSet());
    }

    private static Builder builder() {
        return new Builder();
    }

    /** Readable test queries: only the parts a test sets differ from "everything on sale". */
    private static final class Builder {
        private String category;
        private List<String> categories = List.of();
        private List<Gender> genders = List.of();
        private List<Sport> sports = List.of();
        private List<ColorFamily> colors = List.of();
        private List<String> sizes = List.of();
        private List<String> prices = List.of();
        private boolean onSale;
        private ProductSort sort;
        private int page = 1;

        Builder copy() {
            Builder copy = new Builder();
            copy.category = category;
            copy.categories = categories;
            copy.genders = genders;
            copy.sports = sports;
            copy.colors = colors;
            copy.sizes = sizes;
            copy.prices = prices;
            copy.onSale = onSale;
            copy.sort = sort;
            return copy;
        }

        Builder category(String value) {
            category = value;
            return this;
        }

        Builder categories(String... values) {
            categories = List.of(values);
            return this;
        }

        Builder genders(Gender... values) {
            genders = List.of(values);
            return this;
        }

        Builder sports(Sport... values) {
            sports = List.of(values);
            return this;
        }

        Builder colors(ColorFamily... values) {
            colors = List.of(values);
            return this;
        }

        Builder sizes(String... values) {
            sizes = List.of(values);
            return this;
        }

        Builder prices(String... values) {
            prices = List.of(values);
            return this;
        }

        Builder onSale() {
            onSale = true;
            return this;
        }

        Builder sort(ProductSort value) {
            sort = value;
            return this;
        }

        Builder page(int value) {
            page = value;
            return this;
        }

        ProductListQuery build() {
            return new ProductListQuery(
                    category,
                    categories,
                    genders,
                    sports,
                    colors,
                    sizes,
                    prices,
                    onSale,
                    sort,
                    page);
        }
    }
}
