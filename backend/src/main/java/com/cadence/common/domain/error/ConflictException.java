package com.cadence.common.domain.error;

/** The request is valid but clashes with the current state (for example the price changed meanwhile). */
public abstract class ConflictException extends DomainException {

    protected ConflictException(String code, String message) {
        super(code, message);
    }
}
