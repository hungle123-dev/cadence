package com.cadence.catalog.domain.exception;

import com.cadence.common.domain.error.ResourceNotFoundException;

/** Unknown slug, or a draft or archived product (NFR-SEO-06); both look the same. */
public class ProductNotFoundException extends ResourceNotFoundException {

    public ProductNotFoundException() {
        super(CatalogErrorCode.PRODUCT_NOT_FOUND);
    }
}
