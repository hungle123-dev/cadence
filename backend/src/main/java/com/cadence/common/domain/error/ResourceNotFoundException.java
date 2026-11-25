package com.cadence.common.domain.error;

/** The thing asked for does not exist, or must not be seen (for example a draft product). */
public abstract class ResourceNotFoundException extends DomainException {

    protected ResourceNotFoundException(ErrorCode errorCode) {
        super(requireCategory(errorCode, ErrorCategory.NOT_FOUND));
    }
}
