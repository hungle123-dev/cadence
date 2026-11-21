package com.cadence.catalog.application;

import static com.cadence.catalog.application.ProductFixtures.NOW;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.cadence.catalog.application.dto.CategoryView;
import com.cadence.catalog.application.dto.ProductSort;
import com.cadence.catalog.application.dto.ProductTileQuery;
import com.cadence.catalog.application.dto.SitemapView;
import com.cadence.catalog.application.port.driven.CategoryReadPort;
import com.cadence.catalog.application.service.ProductDetailViewAssembler;
import com.cadence.catalog.application.service.ProductQueryService;
import com.cadence.catalog.application.service.ProductTileViewAssembler;
import com.cadence.catalog.domain.exception.CategoryNotFoundException;

/** PLP and sitemap use case: which categories it asks for (AC 1.2, 1.9) and at what moment. SQL rules are tested on PostgreSQL. */
class ProductListQueryServiceTest {

    private static final UUID GIAY = UUID.fromString("00000000-0000-7000-8000-000000000001");
    private static final UUID GIAY_CHAY_BO = UUID.fromString("00000000-0000-7000-8000-000000000011");
    private static final UUID GIAY_TRAIL = UUID.fromString("00000000-0000-7000-8000-000000000111");
    private static final UUID AO = UUID.fromString("00000000-0000-7000-8000-000000000002");

    /** Giày > Giày chạy bộ > Giày trail, plus an unrelated Áo. */
    private static final CategoryReadPort CATEGORIES = () -> List.of(
            new CategoryView(GIAY, null, "giay", "Giày", 1, 1),
            new CategoryView(AO, null, "ao", "Áo", 1, 2),
            new CategoryView(GIAY_CHAY_BO, GIAY, "giay-chay-bo", "Giày chạy bộ", 2, 1),
            new CategoryView(GIAY_TRAIL, GIAY_CHAY_BO, "giay-trail", "Giày trail", 3, 1));

    private final RecordingTileReadPort tiles = new RecordingTileReadPort();
    private Instant sitemapAskedAt;
    private final ProductQueryService service = new ProductQueryService(
            slug -> Optional.empty(), new ProductDetailViewAssembler(CATEGORIES), tiles, CATEGORIES,
            new ProductTileViewAssembler(), now -> {
                sitemapAskedAt = now;
                return new SitemapView(List.of(), List.of());
            }, Clock.fixed(NOW, ZoneOffset.UTC));

    @Test
    void no_category_searches_every_category() {
        service.listProducts(query(null));

        assertThat(tiles.lastCategoryIds).isEmpty();
    }

    @Test
    void parent_category_includes_children_at_any_depth() {
        service.listProducts(query("giay"));

        assertThat(tiles.lastCategoryIds).containsExactlyInAnyOrder(GIAY, GIAY_CHAY_BO, GIAY_TRAIL);
    }

    @Test
    void leaf_category_is_only_itself() {
        service.listProducts(query("giay-trail"));

        assertThat(tiles.lastCategoryIds).containsExactly(GIAY_TRAIL);
    }

    @Test
    void unknown_category_is_not_found_and_nothing_is_searched() {
        assertThatThrownBy(() -> service.listProducts(query("khong-co-that")))
                .isInstanceOf(CategoryNotFoundException.class);
        assertThat(tiles.calls).isZero();
    }

    @Test
    void search_uses_the_clock() {
        service.listProducts(query(null));

        assertThat(tiles.lastNow).isEqualTo(NOW);
    }

    @Test
    void page_comes_back_from_the_query() {
        var query = new ProductTileQuery(null, List.of(), List.of(), null, null, null, ProductSort.NEWEST, 3);

        assertThat(service.listProducts(query).page()).isEqualTo(3);
    }

    @Test
    void sitemap_asks_the_port_at_the_clock_time() {
        service.getSitemap();

        assertThat(sitemapAskedAt).isEqualTo(NOW);
    }

    private static ProductTileQuery query(String categorySlug) {
        return new ProductTileQuery(categorySlug, List.of(), List.of(), null, null, null, ProductSort.NEWEST, 1);
    }
}
