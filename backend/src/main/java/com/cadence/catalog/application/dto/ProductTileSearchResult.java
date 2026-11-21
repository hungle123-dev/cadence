package com.cadence.catalog.application.dto;

import java.util.List;

import org.jspecify.annotations.Nullable;

import com.cadence.catalog.domain.model.ColorwayPricing;

/** One page of tiles as stored: what ProductTileReadPort.search returns. Pricing is raw; the use case applies R-PRICE. */
public record ProductTileSearchResult(List<Tile> tiles, long totalItems) {

    public record Tile(
            String productSlug,
            String colorwaySlug,
            String productName,
            String colorwayName,
            ProductSnapshot.@Nullable Image image,
            ColorwayPricing pricing,
            int colorCount,
            boolean inStock) {
    }
}
