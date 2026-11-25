package com.cadence.catalog.domain.valueobject;

/**
 * What the customer pays at one moment (BR-PRC-01), in whole VND.
 *
 * @param compareAt the list price, shown struck through; null when no sale is active
 */
public record EffectivePrice(long amount, Long compareAt) {

    private static final int PERCENT = 100;

    public boolean onSale() {
        return compareAt != null;
    }

    /** FR-CAT-09 "−N%" label: rounded down, so the label never promises more than the real cut. */
    public int discountPercent() {
        if (compareAt == null) {
            return 0;
        }
        return (int) ((compareAt - amount) * PERCENT / compareAt);
    }
}
