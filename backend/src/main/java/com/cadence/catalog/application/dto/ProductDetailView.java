package com.cadence.catalog.application.dto;

import java.util.List;
import java.util.Map;

import com.cadence.catalog.domain.model.ColorFamily;
import com.cadence.catalog.domain.model.EffectivePrice;
import com.cadence.catalog.domain.model.Gender;
import com.cadence.catalog.domain.model.ProductType;

/**
 * What ProductQueryUseCase.getProductDetail returns: a visible product (R-VIS) with prices already worked out
 * for "now" (R-PRICE). The web layer only turns it into JSON (storage keys become URLs there, A6).
 */
public record ProductDetailView(
        String slug,
        String name,
        String description,
        ProductType type,
        Gender gender,
        Map<String, String> specs,
        List<Breadcrumb> breadcrumb,
        List<Colorway> colorways) {

    /** Root category first. */
    public record Breadcrumb(String slug, String name) {
    }

    /** Ordered by position; the first is the default colorway (AC 3.9). */
    public record Colorway(
            String slug,
            String name,
            ColorFamily colorFamily,
            EffectivePrice price,
            boolean inStock,
            List<ProductSnapshot.Image> images,
            List<ProductSnapshot.Size> sizes) {
    }
}
