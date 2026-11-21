package com.cadence.catalog.domain.model;

import java.time.Instant;

/** Price of one colorway: every size costs the same (step 3). sale may be null. */
public record ColorwayPricing(long listPrice, Sale sale) {

    public ColorwayPricing {
        if (listPrice <= 0) {
            throw new IllegalArgumentException("listPrice must be > 0");
        }
        if (sale != null && sale.price() >= listPrice) {
            throw new IllegalArgumentException("sale.price() must be < listPrice");
        }
    }

    public EffectivePrice effectivePriceAt(Instant at) {
        if (sale != null && sale.isActiveAt(at)) {
            return new EffectivePrice(sale.price(), listPrice);
        }
        return new EffectivePrice(listPrice, null);
    }
}
