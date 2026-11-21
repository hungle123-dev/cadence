package com.cadence.catalog.application.port.driven;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

import com.cadence.catalog.application.dto.ProductTilePageView;
import com.cadence.catalog.application.dto.ProductTileQuery;
import com.cadence.catalog.application.dto.ProductTileSearchResult;

public interface ProductTileReadPort {

    /**
     * One page of tiles. Unlike ProductDetailReadPort, this one applies R-VIS, R-STOCK (size filter) and
     * R-PRICE (price filter and sort) in SQL, because paging needs the filtered total.
     * An empty categoryIds means every category.
     */
    ProductTileSearchResult search(ProductTileQuery query, Set<UUID> categoryIds, Instant now);

    /** Filter values of those categories, not narrowed by the current filters (A1). */
    ProductTilePageView.FilterOptions findFilterOptions(Set<UUID> categoryIds, Instant now);
}
