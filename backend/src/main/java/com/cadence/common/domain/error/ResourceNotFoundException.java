package com.cadence.common.domain.error;

public abstract class ResourceNotFoundException extends DomainException {

    protected ResourceNotFoundException(ErrorCode code) {
        super(requireCategory(code, ErrorCategory.NOT_FOUND));
    }
}
