package com.cadence.catalog.infrastructure.restapi.uidto;

import com.cadence.catalog.application.dto.ProductSnapshot;

import io.swagger.v3.oas.annotations.media.Schema;

/** 7.5 A6: a URL served by Nginx from SeaweedFS, never the storage key. Width/height avoid layout shift (AC 3.7). */
@Schema(name = "Image")
public record ImageResponse(
        @Schema(example = "/media/products/tempo-2/den-1.jpg") String url,
        @Schema(example = "Giày chạy bộ Tempo 2 màu đen, nhìn từ bên hông") String alt,
        @Schema(example = "1200") int width,
        @Schema(example = "1200") int height) {

    /** Nginx serves SeaweedFS under /media (ADR-0007, A6). */
    private static final String MEDIA_PREFIX = "/media/";

    public static ImageResponse from(ProductSnapshot.Image image) {
        return new ImageResponse(MEDIA_PREFIX + image.storageKey(), image.alt(), image.width(), image.height());
    }
}
