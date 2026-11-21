package com.cadence.catalog.domain.exception;

import java.util.List;

import com.cadence.common.domain.error.InvalidParam;
import com.cadence.common.domain.error.ValidationException;

/** AC 2.11: a PLP filter or sort value the API does not accept. */
public class InvalidProductFilterException extends ValidationException {

    public static final String CODE = "CATALOG_INVALID_FILTER";

    public InvalidProductFilterException(List<InvalidParam> invalidParams) {
        super(CODE, "Bộ lọc không hợp lệ", invalidParams);
    }
}
