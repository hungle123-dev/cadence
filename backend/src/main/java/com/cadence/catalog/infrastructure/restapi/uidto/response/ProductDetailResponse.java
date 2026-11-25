package com.cadence.catalog.infrastructure.restapi.uidto.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.jspecify.annotations.Nullable;

import com.cadence.catalog.application.dto.ProductDetailView;
import com.cadence.catalog.domain.model.ColorFamily;
import com.cadence.catalog.domain.model.Fit;
import com.cadence.catalog.domain.model.Gender;
import com.cadence.catalog.domain.model.Sport;
import com.cadence.catalog.infrastructure.config.ImageUrls;

import io.swagger.v3.oas.annotations.media.Schema;

/** The PDP (FR-CAT-05, FR-CAT-07); a draft or archived product is a 404 instead (NFR-SEO-06). */
@Schema(name = "ProductDetail")
public record ProductDetailResponse(
        @Schema(example = "tempo-2") String slug,
        @Schema(example = "Cadence Tempo 2") String name,
        CategoryResponse category,
        Gender gender,
        Sport sport,
        String description,
        List<SpecResponse> specs,
        @Schema(description = "Size advice: SMALL → half a size up, LARGE → half a size down")
                Fit fit,
        @Schema(
                        description = "Sample data (SRS A-03)",
                        types = {"number", "null"})
                @Nullable BigDecimal rating,
        int reviewCount,
        PriceResponse price,
        boolean inStock,
        boolean isNew,
        @Schema(
                        description =
                                "COMING_SOON: show a countdown to launchAt, block buying, add"
                                        + " noindex (FR-CAT-07)",
                        allowableValues = {"ON_SALE", "COMING_SOON"})
                String visibility,
        Instant launchAt,
        SeoResponse seo,
        @Schema(description = "In display order; the first is the default")
                List<ColorwayResponse> colorways) {

    public ProductDetailResponse {
        specs = List.copyOf(specs);
        colorways = List.copyOf(colorways);
    }

    public static ProductDetailResponse from(ProductDetailView view, ImageUrls urls) {
        return new ProductDetailResponse(
                view.slug(),
                view.name(),
                CategoryResponse.from(view.category()),
                view.gender(),
                view.sport(),
                view.description(),
                view.specs().stream()
                        .map(spec -> new SpecResponse(spec.name(), spec.value()))
                        .toList(),
                view.fit(),
                view.rating(),
                view.reviewCount(),
                PriceResponse.from(view.price()),
                view.inStock(),
                view.isNew(),
                view.visibility().name(),
                view.launchAt(),
                new SeoResponse(view.seoTitle(), view.seoDescription()),
                view.colorways().stream()
                        .map(colorway -> ColorwayResponse.from(colorway, urls))
                        .toList());
    }

    @Schema(name = "Spec")
    public record SpecResponse(
            @Schema(example = "Trọng lượng") String name,
            @Schema(example = "228 g") String value) {}

    /** NFR-SEO-03: the admin override, or the default worked out by the product. */
    @Schema(name = "Seo")
    public record SeoResponse(
            @Schema(example = "Cadence Tempo 2 – Giày chạy bộ | Cadence") String title,
            String description) {}

    @Schema(name = "Colorway")
    public record ColorwayResponse(
            UUID id,
            @Schema(example = "Đen / Trắng") String name,
            ColorFamily colorFamily,
            boolean inStock,
            List<ImageResponse> images,
            @Schema(description = "In size order") List<SizeResponse> sizes) {

        public ColorwayResponse {
            images = List.copyOf(images);
            sizes = List.copyOf(sizes);
        }

        static ColorwayResponse from(ProductDetailView.ColorwayView colorway, ImageUrls urls) {
            return new ColorwayResponse(
                    colorway.id(),
                    colorway.name(),
                    colorway.colorFamily(),
                    colorway.inStock(),
                    colorway.images().stream()
                            .map(image -> ImageResponse.from(image, urls))
                            .toList(),
                    colorway.sizes().stream()
                            .map(variant ->
                                    new SizeResponse(
                                            variant.sku(), variant.size(), variant.available()))
                            .toList());
        }
    }

    /**
     * One size. {@code available} comes from the HTML cache (up to 5 minutes old, architecture
     * 5.1); the page asks for fresh stock when it opens.
     */
    @Schema(name = "Size")
    public record SizeResponse(
            @Schema(example = "TEMPO2-02-42") String sku,
            @Schema(example = "42") String size,
            @Schema(description = "False: shown but cannot be chosen (FR-CAT-06)")
                    boolean available) {}
}
