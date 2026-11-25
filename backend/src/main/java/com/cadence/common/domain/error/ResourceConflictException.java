package com.cadence.common.domain.error;

/** The request is valid but clashes with the current state (for example the price changed). */
public abstract class ResourceConflictException extends DomainException {

    protected ResourceConflictException(ErrorCode errorCode) {
        super(requireCategory(errorCode, ErrorCategory.CONFLICT));
    }
}
