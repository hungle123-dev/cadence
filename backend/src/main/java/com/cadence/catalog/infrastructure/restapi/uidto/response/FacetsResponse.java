package com.cadence.catalog.infrastructure.restapi.uidto.response;

import java.util.List;

import org.jspecify.annotations.Nullable;

import com.cadence.catalog.application.dto.ProductFacetsView;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * The filter panel (FR-CAT-02). Each group is counted with every other chosen filter applied but
 * not its own, so a count tells how many products choosing that value would add.
 */
@Schema(name = "Facets")
public record FacetsResponse(
        @Schema(
                        description = "Absent on a category page, where the category is fixed",
                        types = {"array", "null"})
                @Nullable List<FacetResponse> categories,
        List<FacetResponse> genders,
        List<FacetResponse> sports,
        List<FacetResponse> colors,
        @Schema(description = "Sizes in stock, in size order") List<FacetResponse> sizes,
        @Schema(description = "Every configured band, in order, even at 0")
                List<FacetResponse> prices,
        @Schema(description = "Products with an active sale") long onSale) {

    public FacetsResponse {
        categories = categories == null ? null : List.copyOf(categories);
        genders = List.copyOf(genders);
        sports = List.copyOf(sports);
        colors = List.copyOf(colors);
        sizes = List.copyOf(sizes);
        prices = List.copyOf(prices);
    }

    public static FacetsResponse from(ProductFacetsView facets) {
        var categories = facets.categories();
        return new FacetsResponse(
                categories == null ? null : FacetResponse.all(categories),
                FacetResponse.all(facets.genders()),
                FacetResponse.all(facets.sports()),
                FacetResponse.all(facets.colors()),
                FacetResponse.all(facets.sizes()),
                FacetResponse.all(facets.prices()),
                facets.onSale());
    }

    /** One choice: send {@code value} back as the filter parameter. */
    @Schema(name = "Facet")
    public record FacetResponse(
            @Schema(example = "BLACK") String value,
            @Schema(
                            description = "Display name of a category; null for other groups",
                            types = {"string", "null"})
                    @Nullable String label,
            @Schema(example = "12") long count) {

        static List<FacetResponse> all(List<ProductFacetsView.FacetCount> counts) {
            return counts.stream()
                    .map(count -> new FacetResponse(count.value(), count.label(), count.count()))
                    .toList();
        }
    }
}
