package com.cadence.catalog.infrastructure.restapi.uidto;

import java.util.List;

import org.jspecify.annotations.Nullable;

import com.cadence.catalog.domain.model.ColorFamily;
import com.cadence.catalog.domain.model.Gender;

import io.swagger.v3.oas.annotations.media.Schema;

/** PLP page (CAT-01, CAT-02): one tile per colorway, plus the filter values of the category (A1). */
@Schema(name = "ProductTilePage")
public record ProductTilePageResponse(
        List<ProductTile> items,
        @Schema(description = "1-based", example = "1") int page,
        @Schema(example = "24") int pageSize,
        @Schema(example = "30") long totalItems,
        @Schema(example = "2") int totalPages,
        FilterOptions filters) {

    @Schema(name = "ProductTile")
    public record ProductTile(
            @Schema(example = "tempo-2") String productSlug,
            @Schema(example = "den") String colorwaySlug,
            @Schema(example = "Tempo 2") String productName,
            @Schema(example = "Đen") String colorwayName,
            @Schema(description = "First image of the colorway; null when it has none", types = { "object", "null" }) @Nullable ImageResponse image,
            PriceResponse price,
            @Schema(description = "Visible colorways of the product", example = "3") int colorCount,
            @Schema(description = "At least one size in stock (R-STOCK, AC 1.11)") boolean inStock) {
    }

    @Schema(name = "FilterOptions", description = "Values present in the category, not narrowed by the current filters (A1)")
    public record FilterOptions(
            @Schema(description = "In size_order (AC 2.12)", example = "[\"40\", \"41\", \"42\"]") List<String> sizes,
            List<ColorFamily> colors,
            List<Gender> genders,
            PriceRange priceRange) {
    }

    @Schema(name = "PriceRange", description = "Lowest and highest effective price in the category")
    public record PriceRange(
            @Schema(example = "390000") long min,
            @Schema(example = "3290000") long max) {
    }
}
