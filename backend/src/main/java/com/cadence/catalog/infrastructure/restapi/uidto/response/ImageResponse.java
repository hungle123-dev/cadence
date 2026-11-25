package com.cadence.catalog.infrastructure.restapi.uidto.response;

import com.cadence.catalog.domain.model.ProductImage;
import com.cadence.catalog.infrastructure.config.ImageUrls;

import io.swagger.v3.oas.annotations.media.Schema;

/** A URL built from the image key, never the key itself (BR-CAT-01). */
@Schema(name = "Image")
public record ImageResponse(
        @Schema(example = "https://images.unsplash.com/photo-1631542156377-473540702434")
                String url,
        @Schema(description = "Lets the page reserve the space (NFR-03)", example = "1600")
                int width,
        @Schema(example = "1600") int height,
        @Schema(example = "Cadence Tempo 2, màu Đen / Trắng, ảnh 1") String alt) {

    public static ImageResponse from(ProductImage image, ImageUrls urls) {
        return new ImageResponse(
                urls.urlOf(image.imageKey()), image.width(), image.height(), image.alt());
    }
}
