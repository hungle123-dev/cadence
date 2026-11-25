package com.cadence.catalog.application.port.driving;

import com.cadence.catalog.application.dto.PriceBands;
import com.cadence.catalog.application.dto.ProductDetailView;
import com.cadence.catalog.application.dto.ProductFacetsView;
import com.cadence.catalog.application.dto.ProductListQuery;
import com.cadence.catalog.application.dto.ProductTileView;
import com.cadence.catalog.application.dto.SitemapView;
import com.cadence.common.application.PageResult;

/** Every read of products: PDP, PLP and its filter panel, sitemap. */
public interface ProductQueryUseCase {

    /**
     * @throws com.cadence.catalog.domain.exception.ProductNotFoundException unknown slug, draft or
     *     archived (NFR-SEO-06)
     */
    ProductDetailView getProductDetail(String slug);

    /**
     * @throws com.cadence.catalog.domain.exception.CategoryNotFoundException unknown category page
     * @throws com.cadence.catalog.domain.exception.InvalidProductFilterException unknown category
     *     filter or price band
     */
    PageResult<ProductTileView> listProducts(ProductListQuery query);

    /** Same checks as {@link #listProducts}. */
    ProductFacetsView getFacets(ProductListQuery query);

    /** The configured price bands, for the filter panel. */
    PriceBands getPriceBands();

    SitemapView getSitemap();
}
