package com.cadence.common.domain.error;

/**
 * What kind of error this is, independent of any protocol. Domain code picks a category, never an
 * HTTP status: the web layer maps one to the other in one place ({@code GlobalExceptionHandler}),
 * so the shared kernel stays free of HTTP.
 */
public enum ErrorCategory {
    /** The input breaks a rule. */
    VALIDATION,
    /** The thing asked for does not exist, or must not be seen (a draft product). */
    NOT_FOUND,
    /** The request clashes with the current state (price changed, someone else saved first). */
    CONFLICT,
    /** A business rule refuses the action although the input is well formed. */
    BUSINESS_RULE,
    /** A bug or an outage: details go to the log, never to the client. */
    INTERNAL
}
