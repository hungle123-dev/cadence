package com.cadence.common.domain.util;

/** A failed check is a programming error: IllegalArgumentException, not a business error. */
public final class Preconditions {

    private Preconditions() {
    }

    public static <T> T requireNonNull(T value, String field) {
        if (value == null) throw new IllegalArgumentException(field + " must not be null");
        return value;
    }

    public static String requireNotBlank(String value, String field) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException(field + " must not be blank");
        return value;
    }
}
