package com.cadence.catalog.application;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.cadence.catalog.application.dto.CategoryView;
import com.cadence.catalog.application.dto.ProductSnapshot;
import com.cadence.catalog.application.port.driven.CategoryReadPort;
import com.cadence.catalog.domain.model.ColorFamily;
import com.cadence.catalog.domain.model.ColorwayPricing;
import com.cadence.catalog.domain.model.Gender;
import com.cadence.catalog.domain.model.ProductPublication;
import com.cadence.catalog.domain.model.ProductStatus;
import com.cadence.catalog.domain.model.ProductType;

/** Small builders for catalog application tests: each test states only what it cares about. */
final class ProductFixtures {

    static final Instant NOW = Instant.parse("2026-12-04T12:00:00Z");

    static final UUID GIAY_ID = UUID.fromString("00000000-0000-7000-8000-000000000001");
    static final UUID GIAY_CHAY_BO_ID = UUID.fromString("00000000-0000-7000-8000-000000000011");

    /** Fake port: "Giày" with child "Giày chạy bộ". */
    static final CategoryReadPort CATEGORIES = () -> List.of(
            new CategoryView(GIAY_ID, null, "giay", "Giày", 1, 1),
            new CategoryView(GIAY_CHAY_BO_ID, GIAY_ID, "giay-chay-bo", "Giày chạy bộ", 2, 1));

    static ProductPublication published() {
        return new ProductPublication(ProductStatus.PUBLISHED, NOW.minusSeconds(86_400));
    }

    static ProductSnapshot product(String slug, ProductPublication publication, ProductSnapshot.Colorway... colorways) {
        return new ProductSnapshot(slug, "Cadence Tempo 2", "Giày chạy bộ hằng ngày.", ProductType.SHOES, Gender.UNISEX,
                Map.of("Trọng lượng", "245 g"), GIAY_CHAY_BO_ID, publication, List.of(colorways));
    }

    static ProductSnapshot.Colorway colorway(String slug, ColorwayPricing pricing, ProductSnapshot.Size... sizes) {
        return new ProductSnapshot.Colorway(slug, slug, ColorFamily.BLACK, pricing,
                List.of(new ProductSnapshot.Image("products/tempo-2/" + slug + "-1.jpg", "Ảnh " + slug, 1200, 1200)),
                List.of(sizes));
    }

    /** A colorway with no sale and one size in stock, for tests that do not care about either. */
    static ProductSnapshot.Colorway colorway(String slug) {
        return colorway(slug, new ColorwayPricing(2_490_000, null), size("40", true));
    }

    static ProductSnapshot.Size size(String size, boolean inStock) {
        return new ProductSnapshot.Size("TEMPO2-" + size, size, inStock);
    }

    private ProductFixtures() {
    }
}
