package com.cadence.common.domain.error;

/** Kind of error, independent of HTTP. Only the web layer maps it to a status. */
public enum ErrorCategory {
    VALIDATION,
    NOT_FOUND,
    CONFLICT,
    BUSINESS_RULE,
    INTERNAL
}
