package com.cadence.common.domain.error;

/**
 * A machine-readable error. Each module declares one enum {@code <Module>ErrorCode implements
 * ErrorCode}, so its codes, messages and categories live in one place, inside the module.
 *
 * <p>No i18n message key: Cadence sells in Vietnam only (SRS C-04), so the default message is the
 * Vietnamese text the customer reads.
 */
public interface ErrorCode {

    /** Stable code the client branches on, never on the message: CATALOG_PRODUCT_NOT_FOUND. */
    String getCode();

    /** Text that is safe to show to the customer. */
    String getDefaultMessage();

    ErrorCategory getCategory();
}
