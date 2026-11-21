package com.cadence.catalog.application.port.driving;

import com.cadence.catalog.application.dto.ProductDetailView;

/** Every read of products (7.5b): PDP now; PLP and sitemap join it in step 6. */
public interface ProductQueryUseCase {

    /** @throws com.cadence.catalog.domain.exception.ProductNotFoundException unknown slug or not visible (A7) */
    ProductDetailView getProductDetail(String slug);
}
