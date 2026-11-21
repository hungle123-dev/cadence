package com.cadence.common.domain.error;

import java.util.Objects;

/**
 * Base of every business error (template section 3: common/domain/error).
 *
 * <p>Carries a stable machine code such as {@code CATALOG_PRODUCT_NOT_FOUND} and a message that is
 * safe to show to the customer. It knows nothing about HTTP: the web layer picks the status from the
 * subtype ({@link NotFoundException}, {@link ValidationException}, {@link ConflictException}).
 * Each module declares one class per situation in its own {@code domain/exception} package.
 */
public abstract class DomainException extends RuntimeException {

    private final String code;

    protected DomainException(String code, String message) {
        super(message);
        this.code = Objects.requireNonNull(code, "code");
    }

    public String code() {
        return code;
    }
}
