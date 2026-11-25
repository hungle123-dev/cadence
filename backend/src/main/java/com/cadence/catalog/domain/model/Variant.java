package com.cadence.catalog.domain.model;

import java.util.UUID;

import com.cadence.common.domain.util.Preconditions;

/**
 * One size of a colorway, the unit a customer buys (one SKU).
 *
 * <p>{@code available} is the catalog's copy of "ATS > 0" kept from inventory (architecture 7.8):
 * the catalog never reads inventory's tables (database P2), it only keeps a yes / no per variant.
 */
public record Variant(UUID id, String sku, String size, boolean available) {

    public Variant {
        Preconditions.requireNonNull(id, "id");
        Preconditions.requireNotBlank(sku, "sku");
        Preconditions.requireNotBlank(size, "size");
    }
}
