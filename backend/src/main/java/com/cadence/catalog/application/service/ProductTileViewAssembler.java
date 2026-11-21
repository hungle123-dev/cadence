package com.cadence.catalog.application.service;

import java.time.Instant;

import com.cadence.catalog.application.dto.ProductTilePageView;
import com.cadence.catalog.application.dto.ProductTileQuery;
import com.cadence.catalog.application.dto.ProductTileSnapshotPage;

/** PLP view: works out each tile's price for "now" (R-PRICE) and the page count. Pure: no port, no clock. */
public class ProductTileViewAssembler {

    public ProductTilePageView toView(ProductTileSnapshotPage snapshotPage, ProductTilePageView.FilterOptions filters,
            int page, Instant now) {
        var items = snapshotPage.tiles().stream()
                .map(tile -> toTile(tile, now))
                .toList();
        return new ProductTilePageView(items, page, ProductTileQuery.PAGE_SIZE, snapshotPage.totalItems(),
                totalPages(snapshotPage.totalItems()), filters);
    }

    private static ProductTilePageView.Tile toTile(ProductTileSnapshotPage.Tile tile, Instant now) {
        return new ProductTilePageView.Tile(tile.productSlug(), tile.colorwaySlug(), tile.productName(),
                tile.colorwayName(), tile.image(), tile.pricing().effectivePriceAt(now), tile.colorCount(),
                tile.inStock());
    }

    /** Rounded up; no items means no pages. */
    private static int totalPages(long totalItems) {
        return (int) ((totalItems + ProductTileQuery.PAGE_SIZE - 1) / ProductTileQuery.PAGE_SIZE);
    }
}
