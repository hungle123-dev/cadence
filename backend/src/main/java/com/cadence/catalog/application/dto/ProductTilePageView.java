package com.cadence.catalog.application.dto;

import java.util.List;

import org.jspecify.annotations.Nullable;

import com.cadence.catalog.domain.model.ColorFamily;
import com.cadence.catalog.domain.model.EffectivePrice;
import com.cadence.catalog.domain.model.Gender;

/**
 * What ProductQueryUseCase.listProducts returns: one tile per colorway of a visible product (R-VIS), prices
 * already worked out for "now" (R-PRICE). The web layer only turns it into JSON (A6).
 */
public record ProductTilePageView(
        List<Tile> items,
        int page,
        int pageSize,
        long totalItems,
        int totalPages,
        FilterOptions filters) {

    public record Tile(
            String productSlug,
            String colorwaySlug,
            String productName,
            String colorwayName,
            ProductSnapshot.@Nullable Image image,
            EffectivePrice price,
            int colorCount,
            boolean inStock) {
    }

    /** Values present in the categories, not narrowed by the current filters (A1). Sizes in size_order (AC 2.12). */
    public record FilterOptions(
            List<String> sizes,
            List<ColorFamily> colors,
            List<Gender> genders,
            PriceRange priceRange) {
    }

    /** Lowest and highest effective price. */
    public record PriceRange(long min, long max) {
    }
}
