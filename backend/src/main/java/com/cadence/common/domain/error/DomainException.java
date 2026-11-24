package com.cadence.common.domain.error;

import com.cadence.common.domain.util.Preconditions;
import lombok.Getter;

/** Base of every business error. Errors of category BUSINESS_RULE extend this class directly. */
@Getter
public abstract class DomainException extends RuntimeException {

    private final ErrorCode errorCode;

    protected DomainException(ErrorCode errorCode) {
        this(errorCode, Preconditions.requireNonNull(errorCode, "errorCode").getDefaultMessage());
    }

    protected DomainException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = Preconditions.requireNonNull(errorCode, "errorCode");
    }

    /** A "not found" exception can never carry a VALIDATION code. */
    static ErrorCode requireCategory(ErrorCode code, ErrorCategory expected) {
        Preconditions.requireNonNull(code, "errorCode");
        if (code.getCategory() != expected) {
            throw new IllegalArgumentException(code.getCode() + " is not " + expected);
        }
        return code;
    }
}
