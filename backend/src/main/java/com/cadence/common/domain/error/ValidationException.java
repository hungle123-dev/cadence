package com.cadence.common.domain.error;

import java.util.List;

/** The input breaks a rule; {@link #invalidParams()} says which parameter and why. */
public abstract class ValidationException extends DomainException {

    private final List<InvalidParam> invalidParams;

    protected ValidationException(String code, String message, List<InvalidParam> invalidParams) {
        super(code, message);
        this.invalidParams = List.copyOf(invalidParams);
    }

    public List<InvalidParam> invalidParams() {
        return invalidParams;
    }
}
