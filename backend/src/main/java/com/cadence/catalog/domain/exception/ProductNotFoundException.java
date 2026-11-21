package com.cadence.catalog.domain.exception;

import com.cadence.common.domain.error.NotFoundException;

/** AC 3.8 and A7: unknown slug, or a product that is not visible yet (R-VIS); both look the same. */
public class ProductNotFoundException extends NotFoundException {

    public static final String CODE = "CATALOG_PRODUCT_NOT_FOUND";

    public ProductNotFoundException() {
        super(CODE, "Không tìm thấy sản phẩm");
    }
}
