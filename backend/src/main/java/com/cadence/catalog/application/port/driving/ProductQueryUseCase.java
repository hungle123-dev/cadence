package com.cadence.catalog.application.port.driving;

import com.cadence.catalog.application.dto.ProductDetailView;
import com.cadence.catalog.application.dto.ProductTilePageView;
import com.cadence.catalog.application.dto.ProductTileQuery;

/** Every read of products (7.5b): PDP and PLP; the sitemap joins later. */
public interface ProductQueryUseCase {

    /** @throws com.cadence.catalog.domain.exception.ProductNotFoundException unknown slug or not visible (A7) */
    ProductDetailView getProductDetail(String slug);

    /** @throws com.cadence.catalog.domain.exception.CategoryNotFoundException unknown category slug (AC 1.9) */
    ProductTilePageView listProducts(ProductTileQuery query);
}
