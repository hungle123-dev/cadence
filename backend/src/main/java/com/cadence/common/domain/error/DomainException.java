package com.cadence.common.domain.error;

import com.cadence.common.domain.util.Preconditions;

import lombok.Getter;

/**
 * Base of every business error. Carries an {@link ErrorCode}; the web layer turns its category into
 * a status. Modules extend one of the kinds ({@link ValidationException}, {@link
 * ResourceNotFoundException}, {@link ResourceConflictException}), or this class for a {@link
 * ErrorCategory#BUSINESS_RULE} error: one class per situation in their {@code domain/exception}.
 */
@Getter
public abstract class DomainException extends RuntimeException {

    private final ErrorCode errorCode;

    /** The customer reads the code's default message. */
    protected DomainException(ErrorCode errorCode) {
        this(errorCode, Preconditions.requireNonNull(errorCode, "errorCode").getDefaultMessage());
    }

    /** A more precise message than the code's default, for example naming the thing. */
    protected DomainException(ErrorCode errorCode, String message) {
        // Checked before super(): if it fails, no half-built exception object exists.
        Preconditions.requireNonNull(errorCode, "errorCode");
        super(message);
        this.errorCode = errorCode;
    }

    /**
     * Each kind accepts only codes of its own category, so a class name and its status never
     * disagree (a "not found" exception that answers 400).
     */
    static ErrorCode requireCategory(ErrorCode errorCode, ErrorCategory expected) {
        Preconditions.requireNonNull(errorCode, "errorCode");
        if (errorCode.getCategory() != expected) {
            throw new IllegalArgumentException(
                    "%s has category %s, expected %s"
                            .formatted(errorCode.getCode(), errorCode.getCategory(), expected));
        }
        return errorCode;
    }
}
