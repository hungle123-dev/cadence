package com.cadence.common.domain.error;

import java.util.List;

/** The input breaks a rule; {@link #getInvalidParams()} says which parameter and why. */
public abstract class ValidationException extends DomainException {

    private final List<InvalidParam> invalidParams;

    protected ValidationException(ErrorCode errorCode, List<InvalidParam> invalidParams) {
        super(requireCategory(errorCode, ErrorCategory.VALIDATION));
        this.invalidParams = List.copyOf(invalidParams);
    }

    public List<InvalidParam> getInvalidParams() {
        return invalidParams;
    }
}
