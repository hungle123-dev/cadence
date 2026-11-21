package com.cadence.catalog.domain.model;

/** What the customer pays at a given moment (R-PRICE). compareAt is the struck-through list price, null when not on sale. */
public record EffectivePrice(long amount, Long compareAt) {

    public boolean onSale() {
        return compareAt != null;
    }
}
