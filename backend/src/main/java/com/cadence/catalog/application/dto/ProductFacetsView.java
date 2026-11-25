package com.cadence.catalog.application.dto;

import java.util.List;

import org.jspecify.annotations.Nullable;

/**
 * The filter panel with a count per choice (FR-CAT-02). Each group is counted with every other
 * chosen filter applied but not its own, so choosing "BLACK" still shows how many products are
 * "WHITE": the counts say what choosing a value would add, and the panel never empties itself.
 *
 * @param categories null on a category page, where the category is fixed (FR-CAT-02)
 * @param sizes in size order; only sizes in stock count (FR-CAT-02)
 * @param prices one entry per configured band, in band order
 * @param onSale products with an active sale
 */
public record ProductFacetsView(
        @Nullable List<FacetCount> categories,
        List<FacetCount> genders,
        List<FacetCount> sports,
        List<FacetCount> colors,
        List<FacetCount> sizes,
        List<FacetCount> prices,
        long onSale) {

    public ProductFacetsView {
        categories = categories == null ? null : List.copyOf(categories);
        genders = List.copyOf(genders);
        sports = List.copyOf(sports);
        colors = List.copyOf(colors);
        sizes = List.copyOf(sizes);
        prices = List.copyOf(prices);
    }

    /**
     * One choice of a filter group.
     *
     * @param value what the client sends back as the filter value
     * @param label display name for categories; null for the other groups, whose labels the web app
     *     knows from the value
     */
    public record FacetCount(String value, @Nullable String label, long count) {}
}
