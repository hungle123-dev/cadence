package com.cadence.common.domain.error;

public abstract class ValidationException extends DomainException {

    protected ValidationException(ErrorCode code) {
        super(requireCategory(code, ErrorCategory.VALIDATION));
    }

    protected ValidationException(ErrorCode code, String message) {
        super(requireCategory(code, ErrorCategory.VALIDATION), message);
    }
}
