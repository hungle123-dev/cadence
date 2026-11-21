package com.cadence.catalog.infrastructure.restapi.uidto;

import java.util.List;
import java.util.Map;

import com.cadence.catalog.domain.model.ColorFamily;
import com.cadence.catalog.domain.model.Gender;
import com.cadence.catalog.domain.model.ProductType;

import io.swagger.v3.oas.annotations.media.Schema;

/** PDP (CAT-03): every visible colorway at once, so switching colour needs no new request (A4). */
@Schema(name = "ProductDetail")
public record ProductDetailResponse(
        @Schema(example = "tempo-2") String slug,
        @Schema(example = "Tempo 2") String name,
        String description,
        @Schema(description = "Picks the size chart (AC 3.6)") ProductType type,
        Gender gender,
        @Schema(description = "Free-form specs shown as a table", example = "{\"Trọng lượng\": \"245 g\"}")
        Map<String, String> specs,
        @Schema(description = "Root category first") List<BreadcrumbItem> breadcrumb,
        @Schema(description = "Ordered by position; the first is the default (AC 3.9)") List<ColorwayDetail> colorways) {

    @Schema(name = "ColorwayDetail")
    public record ColorwayDetail(
            @Schema(description = "Used in the web URL ?mau=", example = "den") String slug,
            @Schema(example = "Đen") String name,
            ColorFamily colorFamily,
            PriceResponse price,
            boolean inStock,
            List<ImageResponse> images,
            @Schema(description = "In size_order") List<SizeOption> sizes) {
    }

    @Schema(name = "SizeOption")
    public record SizeOption(
            @Schema(example = "CAD-TEMPO2-BLK-42") String sku,
            @Schema(example = "42") String size,
            @Schema(description = "Out-of-stock sizes are shown but locked (AC 3.3)") boolean inStock) {
    }

    @Schema(name = "BreadcrumbItem")
    public record BreadcrumbItem(
            @Schema(example = "giay-chay-bo") String slug,
            @Schema(example = "Giày chạy bộ") String name) {
    }
}
