package com.cadence.catalog.application;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import com.cadence.catalog.application.dto.ProductTilePageView;
import com.cadence.catalog.application.dto.ProductTileQuery;
import com.cadence.catalog.application.dto.ProductTileSearchResult;
import com.cadence.catalog.application.port.driven.ProductTileReadPort;

/** Fake PLP port: answers with an empty page and remembers what it was asked. */
final class RecordingTileReadPort implements ProductTileReadPort {

    static final ProductTilePageView.FilterOptions NO_FILTERS =
            new ProductTilePageView.FilterOptions(List.of(), List.of(), List.of(), new ProductTilePageView.PriceRange(0, 0));

    int calls;
    Set<UUID> lastCategoryIds;
    Instant lastNow;

    @Override
    public ProductTileSearchResult search(ProductTileQuery query, Set<UUID> categoryIds, Instant now) {
        calls++;
        lastCategoryIds = categoryIds;
        lastNow = now;
        return new ProductTileSearchResult(List.of(), 0);
    }

    @Override
    public ProductTilePageView.FilterOptions findFilterOptions(Set<UUID> categoryIds, Instant now) {
        return NO_FILTERS;
    }
}
