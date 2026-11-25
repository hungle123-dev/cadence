package com.cadence.catalog.domain.exception;

import com.cadence.common.domain.error.ResourceNotFoundException;

/** Unknown category slug. */
public class CategoryNotFoundException extends ResourceNotFoundException {

    public CategoryNotFoundException() {
        super(CatalogErrorCode.CATEGORY_NOT_FOUND);
    }
}
