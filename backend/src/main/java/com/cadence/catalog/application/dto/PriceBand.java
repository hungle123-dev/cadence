package com.cadence.catalog.application.dto;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jspecify.annotations.Nullable;

/**
 * One fixed price range of the PLP filter (FR-CAT-02): {@code min <= price < max}, compared with
 * the effective price. Written {@code "min-max"} in URLs, {@code "min-"} for the open top band.
 *
 * @param max null for the top band
 */
public record PriceBand(long min, @Nullable Long max) {

    private static final Pattern TEXT = Pattern.compile("(\\d{1,15})-(\\d{1,15})?");

    public PriceBand {
        if (min < 0) {
            throw new IllegalArgumentException("min must not be negative");
        }
        if (max != null && max <= min) {
            throw new IllegalArgumentException("max must be above min");
        }
    }

    /** Null when the text is not "min-max" or "min-", or max is not above min. */
    public static @Nullable PriceBand parse(String text) {
        Matcher m = TEXT.matcher(text);
        if (!m.matches()) {
            return null;
        }
        long min = Long.parseLong(m.group(1));
        Long max = m.group(2) == null ? null : Long.valueOf(m.group(2));
        if (max != null && max <= min) {
            return null;
        }
        return new PriceBand(min, max);
    }

    public boolean contains(long price) {
        return price >= min && (max == null || price < max);
    }

    @Override
    public String toString() {
        return min + "-" + (max == null ? "" : max);
    }
}
