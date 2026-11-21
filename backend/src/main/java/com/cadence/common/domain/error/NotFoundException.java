package com.cadence.common.domain.error;

/** The thing asked for does not exist, or must not be seen (for example a draft product). */
public abstract class NotFoundException extends DomainException {

    protected NotFoundException(String code, String message) {
        super(code, message);
    }
}
