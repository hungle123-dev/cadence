package com.cadence.common.domain.util;

/**
 * One-line argument checks that name the field, instead of {@code Objects.requireNonNull} and
 * {@code if ... throw} spread around. A failed check is a programming error, so it throws {@link
 * IllegalArgumentException}, not a business exception.
 */
public final class Preconditions {

    private Preconditions() {}

    public static <T> T requireNonNull(T value, String fieldName) {
        if (value == null) {
            throw new IllegalArgumentException(fieldName + " must not be null");
        }
        return value;
    }

    public static String requireNotBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return value;
    }
}
