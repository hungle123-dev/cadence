package com.cadence.catalog.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.cadence.catalog.application.dto.CategoryView;
import com.cadence.catalog.application.dto.PriceBands;
import com.cadence.catalog.application.dto.ProductFacetsView;
import com.cadence.catalog.application.dto.ProductListQuery;
import com.cadence.catalog.application.dto.ProductTileView;
import com.cadence.catalog.application.dto.SitemapView;
import com.cadence.catalog.application.port.driven.ProductListReadPort;
import com.cadence.catalog.domain.exception.CategoryNotFoundException;
import com.cadence.catalog.domain.exception.InvalidProductFilterException;
import com.cadence.catalog.domain.exception.ProductNotFoundException;
import com.cadence.catalog.domain.model.Fit;
import com.cadence.catalog.domain.model.Gender;
import com.cadence.catalog.domain.model.Product;
import com.cadence.catalog.domain.model.Sport;
import com.cadence.catalog.domain.valueobject.ProductPricing;
import com.cadence.catalog.domain.valueobject.ProductPublication;
import com.cadence.catalog.domain.valueobject.ProductStatus;
import com.cadence.catalog.domain.valueobject.Visibility;
import com.cadence.common.application.PageResult;

class ProductQueryServiceTest {

    private static final Instant NOW = Instant.parse("2026-11-25T03:00:00Z");
    private static final CategoryView RUNNING_SHOES =
            new CategoryView(UUID.randomUUID(), "giay-chay-bo", "Giày chạy bộ");

    private final RecordingListPort listPort = new RecordingListPort();

    @Test
    @DisplayName("TC-CAT-14 Nháp và ngừng bán trả 404 như sản phẩm không tồn tại")
    void getProductDetail_hidesDraftsAndArchivedProducts() {
        assertThatThrownBy(() -> service(product(ProductStatus.DRAFT, null)).getProductDetail("p"))
                .isInstanceOf(ProductNotFoundException.class);
        assertThatThrownBy(() ->
                        service(product(ProductStatus.ARCHIVED, NOW.minusSeconds(60)))
                                .getProductDetail("p"))
                .isInstanceOf(ProductNotFoundException.class);
        assertThatThrownBy(() -> service(null).getProductDetail("missing"))
                .isInstanceOf(ProductNotFoundException.class);
    }

    @Test
    @DisplayName("TC-CAT-06 Sắp mở bán vẫn có PDP, kèm giờ mở bán để đếm ngược")
    void getProductDetail_showsAComingSoonProduct() {
        Instant launch = NOW.plusSeconds(3600);

        var detail = service(product(ProductStatus.PUBLISHED, launch)).getProductDetail("p");

        assertThat(detail.visibility()).isEqualTo(Visibility.COMING_SOON);
        assertThat(detail.launchAt()).isEqualTo(launch);
        assertThat(detail.category()).isEqualTo(RUNNING_SHOES);
        assertThat(detail.seoTitle()).isEqualTo("Tempo – Giày chạy bộ | Cadence");
    }

    @Test
    void listProducts_answers404ForAnUnknownCategoryPage() {
        assertThatThrownBy(
                        () -> service(null).listProducts(query("khong-co", List.of(), List.of())))
                .isInstanceOf(CategoryNotFoundException.class);
        assertThat(listPort.calls).isZero();
    }

    @Test
    @DisplayName("TC-CAT-12 Danh mục lọc không tồn tại và mức giá không cấu hình bị từ chối")
    void listProducts_rejectsUnknownCategoryFiltersAndBands() {
        assertThatThrownBy(() ->
                        service(null)
                                .getFacets(query(null, List.of("khong-co"), List.of("0-500000"))))
                .isInstanceOf(InvalidProductFilterException.class)
                .hasFieldOrProperty("invalidParams");
        assertThat(listPort.calls).isZero();
    }

    @Test
    void listProducts_passesAValidQueryWithTheClockTime() {
        service(null).listProducts(query("giay-chay-bo", List.of(), List.of("1000000-2000000")));

        assertThat(listPort.calls).isEqualTo(1);
        assertThat(listPort.lastNow).isEqualTo(NOW);
    }

    private ProductQueryService service(Product product) {
        return new ProductQueryService(
                slug -> Optional.ofNullable(product),
                listPort,
                () -> List.of(RUNNING_SHOES),
                now -> new SitemapView(List.of(), List.of()),
                PriceBands.fromBoundaries(List.of(1_000_000L, 2_000_000L, 3_000_000L)),
                Clock.fixed(NOW, ZoneOffset.UTC));
    }

    private static Product product(ProductStatus status, Instant launchAt) {
        return new Product(
                UUID.randomUUID(),
                "p",
                "Tempo",
                RUNNING_SHOES.id(),
                Gender.MEN,
                Sport.RUNNING,
                "",
                List.of(),
                Fit.TRUE_TO_SIZE,
                null,
                null,
                0,
                null,
                null,
                new ProductPricing(1_000_000, null),
                new ProductPublication(status, launchAt),
                List.of());
    }

    private static ProductListQuery query(
            String category, List<String> categories, List<String> prices) {
        return new ProductListQuery(
                category,
                categories,
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                prices,
                false,
                null,
                1);
    }

    /** Counts calls, so a test can prove an invalid query never reached the database. */
    private static final class RecordingListPort implements ProductListReadPort {
        private int calls;
        private Instant lastNow;

        @Override
        public PageResult<ProductTileView> findTiles(ProductListQuery query, Instant now) {
            calls++;
            lastNow = now;
            return PageResult.of(List.of(), 1, ProductListQuery.PAGE_SIZE, 0);
        }

        @Override
        public ProductFacetsView findFacets(
                ProductListQuery query, PriceBands priceBands, Instant now) {
            calls++;
            lastNow = now;
            return new ProductFacetsView(
                    null, List.of(), List.of(), List.of(), List.of(), List.of(), 0);
        }
    }
}
