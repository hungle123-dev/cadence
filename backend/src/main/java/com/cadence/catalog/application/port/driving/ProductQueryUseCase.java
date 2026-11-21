package com.cadence.catalog.application.port.driving;

import com.cadence.catalog.application.dto.ProductDetailView;
import com.cadence.catalog.application.dto.ProductTilePageView;
import com.cadence.catalog.application.dto.ProductTileQuery;
import com.cadence.catalog.application.dto.SitemapView;

/** Every read of products (7.5b): PDP, PLP and the sitemap. */
public interface ProductQueryUseCase {

    /** @throws com.cadence.catalog.domain.exception.ProductNotFoundException unknown slug or not visible (A7) */
    ProductDetailView getProductDetail(String slug);

    /** @throws com.cadence.catalog.domain.exception.CategoryNotFoundException unknown category slug (AC 1.9) */
    ProductTilePageView listProducts(ProductTileQuery query);

    /** Slugs for /sitemap.xml (N4, AC 4.6): every category, visible products only. */
    SitemapView sitemap();
}
