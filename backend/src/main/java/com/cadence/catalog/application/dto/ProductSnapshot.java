package com.cadence.catalog.application.dto;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.cadence.catalog.domain.model.ColorFamily;
import com.cadence.catalog.domain.model.ColorwayPricing;
import com.cadence.catalog.domain.model.Gender;
import com.cadence.catalog.domain.model.ProductPublication;
import com.cadence.catalog.domain.model.ProductType;

/** A product as stored, before any rule is applied: what ProductDetailReadPort returns. */
public record ProductSnapshot(
        String slug,
        String name,
        String description,
        ProductType type,
        Gender gender,
        Map<String, String> specs,
        UUID categoryId,
        ProductPublication publication,
        List<Colorway> colorways) {

    /** Ordered by position. */
    public record Colorway(
            String slug,
            String name,
            ColorFamily colorFamily,
            ColorwayPricing pricing,
            List<Image> images,
            List<Size> sizes) {
    }

    public record Image(String storageKey, String alt, int width, int height) {
    }

    /** Ordered by size_order. */
    public record Size(String sku, String size, boolean inStock) {
    }
}
