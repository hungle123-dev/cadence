package com.cadence.catalog.application.port.driven;

import java.time.Instant;

import com.cadence.catalog.application.dto.PriceBands;
import com.cadence.catalog.application.dto.ProductFacetsView;
import com.cadence.catalog.application.dto.ProductListQuery;
import com.cadence.catalog.application.dto.ProductTileView;
import com.cadence.common.application.PageResult;

/**
 * PLP read model. Unlike ProductRepositoryPort it applies R-VIS, R-PRICE and R-STOCK in SQL,
 * because paging and counting need the filtered total. Only products on sale at {@code now} are
 * listed (SRS 6.5).
 */
public interface ProductListReadPort {

    PageResult<ProductTileView> findTiles(ProductListQuery query, Instant now);

    /** Counts per filter choice; prices are counted per configured band. */
    ProductFacetsView findFacets(ProductListQuery query, PriceBands priceBands, Instant now);
}
