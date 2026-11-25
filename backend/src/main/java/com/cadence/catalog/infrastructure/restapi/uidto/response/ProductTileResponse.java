package com.cadence.catalog.infrastructure.restapi.uidto.response;

import org.jspecify.annotations.Nullable;

import com.cadence.catalog.application.dto.ProductTileView;
import com.cadence.catalog.infrastructure.config.ImageUrls;

import io.swagger.v3.oas.annotations.media.Schema;

/** One product of a list (FR-CAT-01); the labels of FR-CAT-09 follow from its fields. */
@Schema(name = "ProductTile")
public record ProductTileResponse(
        @Schema(example = "tempo-2") String slug,
        @Schema(example = "Cadence Tempo 2") String name,
        @Schema(
                        description = "First photo of the first colorway; null when it has none",
                        types = {"object", "null"})
                @Nullable ImageResponse image,
        PriceResponse price,
        @Schema(example = "3") int colorCount,
        @Schema(description = "False shows the \"Hết hàng\" label") boolean inStock,
        @Schema(description = "Launched less than 30 days ago: the \"Mới\" label") boolean isNew) {

    public static ProductTileResponse from(ProductTileView tile, ImageUrls urls) {
        var image = tile.image();
        return new ProductTileResponse(
                tile.slug(),
                tile.name(),
                image == null ? null : ImageResponse.from(image, urls),
                PriceResponse.from(tile.price()),
                tile.colorCount(),
                tile.inStock(),
                tile.isNew());
    }
}
