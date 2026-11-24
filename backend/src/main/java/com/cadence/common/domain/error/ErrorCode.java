package com.cadence.common.domain.error;

/**
 * Machine-readable error. Each module declares one enum {@code <Module>ErrorCode implements ErrorCode}.
 */
public interface ErrorCode {

    /** Stable; the client branches on it, e.g. {@code CATALOG_PRODUCT_NOT_FOUND}. */
    String getCode();

    /** Safe to show to the user. */
    String getDefaultMessage();

    ErrorCategory getCategory();
}
