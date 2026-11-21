package com.cadence.catalog.domain.exception;

import com.cadence.common.domain.error.NotFoundException;

/** AC 1.9: unknown category slug. */
public class CategoryNotFoundException extends NotFoundException {

    public static final String CODE = "CATALOG_CATEGORY_NOT_FOUND";

    public CategoryNotFoundException() {
        super(CODE, "Không tìm thấy danh mục");
    }
}
