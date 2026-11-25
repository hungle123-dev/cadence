package com.cadence.catalog.domain.exception;

import java.util.List;

import com.cadence.common.domain.error.InvalidParam;
import com.cadence.common.domain.error.ValidationException;

/** A list filter, sort or page value the API does not accept; lists every wrong one at once. */
public class InvalidProductFilterException extends ValidationException {

    public InvalidProductFilterException(List<InvalidParam> invalidParams) {
        super(CatalogErrorCode.INVALID_FILTER, invalidParams);
    }
}
