package com.cadence.catalog.application.service;

import java.time.Clock;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import com.cadence.catalog.application.dto.CategoryView;
import com.cadence.catalog.application.dto.ProductDetailView;
import com.cadence.catalog.application.dto.ProductTilePageView;
import com.cadence.catalog.application.dto.ProductTileQuery;
import com.cadence.catalog.application.dto.SitemapView;
import com.cadence.catalog.application.port.driven.CategoryReadPort;
import com.cadence.catalog.application.port.driven.ProductDetailReadPort;
import com.cadence.catalog.application.port.driven.ProductTileReadPort;
import com.cadence.catalog.application.port.driven.SitemapReadPort;
import com.cadence.catalog.application.port.driving.ProductQueryUseCase;
import com.cadence.catalog.domain.exception.CategoryNotFoundException;
import com.cadence.catalog.domain.exception.ProductNotFoundException;

/** Reads of products. Applies R-VIS: a hidden product answers exactly like a missing one (A7). */
public class ProductQueryService implements ProductQueryUseCase {

    private final ProductDetailReadPort productDetails;
    private final ProductDetailViewAssembler detailAssembler;
    private final ProductTileReadPort productTiles;
    private final CategoryReadPort categories;
    private final ProductTileViewAssembler tileAssembler;
    private final SitemapReadPort sitemapEntries;
    private final Clock clock;

    public ProductQueryService(ProductDetailReadPort productDetails, ProductDetailViewAssembler detailAssembler,
            ProductTileReadPort productTiles, CategoryReadPort categories, ProductTileViewAssembler tileAssembler,
            SitemapReadPort sitemapEntries, Clock clock) {
        this.productDetails = productDetails;
        this.detailAssembler = detailAssembler;
        this.productTiles = productTiles;
        this.categories = categories;
        this.tileAssembler = tileAssembler;
        this.sitemapEntries = sitemapEntries;
        this.clock = clock;
    }

    @Override
    public ProductDetailView getProductDetail(String slug) {
        Instant now = clock.instant(); // one moment for both R-VIS and R-PRICE
        return productDetails.findBySlug(slug)
                .filter(product -> product.publication().isVisibleAt(now))
                .map(product -> detailAssembler.toView(product, now))
                .orElseThrow(ProductNotFoundException::new);
    }

    @Override
    public ProductTilePageView listProducts(ProductTileQuery query) {
        Instant now = clock.instant(); // one moment for R-VIS, R-PRICE and the filter options
        Set<UUID> categoryIds = query.categorySlug() == null ? Set.of() : categoryIdsWithChildren(query.categorySlug());
        var tilePage = productTiles.findTiles(query, categoryIds, now);
        var filters = productTiles.findFilterOptions(categoryIds, now);
        return tileAssembler.toView(tilePage, filters, query.page(), now);
    }

    @Override
    public SitemapView getSitemap() {
        return sitemapEntries.findSitemapEntries(clock.instant());
    }

    /** The category and every category below it, at any depth (AC 1.2). */
    private Set<UUID> categoryIdsWithChildren(String slug) {
        var all = categories.findAllOrdered();
        var root = all.stream()
                .filter(category -> category.slug().equals(slug))
                .findFirst()
                .orElseThrow(CategoryNotFoundException::new);

        Set<UUID> ids = new HashSet<>();
        ids.add(root.id());
        boolean added = true;
        while (added) {
            added = false;
            for (CategoryView category : all) {
                if (category.parentId() != null && ids.contains(category.parentId())) {
                    added |= ids.add(category.id());
                }
            }
        }
        return Set.copyOf(ids);
    }
}
