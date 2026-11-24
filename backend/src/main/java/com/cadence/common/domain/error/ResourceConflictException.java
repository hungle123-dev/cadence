package com.cadence.common.domain.error;

public abstract class ResourceConflictException extends DomainException {

    protected ResourceConflictException(ErrorCode code) {
        super(requireCategory(code, ErrorCategory.CONFLICT));
    }

    protected ResourceConflictException(ErrorCode code, String message) {
        super(requireCategory(code, ErrorCategory.CONFLICT), message);
    }
}
