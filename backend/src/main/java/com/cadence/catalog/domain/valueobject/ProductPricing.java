package com.cadence.catalog.domain.valueobject;

import java.time.Instant;

/**
 * Price of a product, the same for every colorway and size (BR-PRC-08).
 *
 * @param sale null when the product has no sale (BR-PRC-09 allows at most one)
 */
public record ProductPricing(long listPrice, Sale sale) {

    public ProductPricing {
        if (listPrice <= 0) {
            throw new IllegalArgumentException("listPrice must be > 0");
        }
        if (sale != null && sale.price() >= listPrice) {
            throw new IllegalArgumentException("sale price must be below the list price");
        }
    }

    /** R-PRICE (BR-PRC-01): the sale price while the sale is active, otherwise the list price. */
    public EffectivePrice priceAt(Instant at) {
        if (sale != null && sale.isActiveAt(at)) {
            return new EffectivePrice(sale.price(), listPrice);
        }
        return new EffectivePrice(listPrice, null);
    }
}
