package com.cadence.catalog.domain.model;

import java.time.Instant;
import java.util.Objects;

/** A time-boxed sale price of a colorway (R-PRICE). Active in [startsAt, endsAt). */
public record Sale(long price, Instant startsAt, Instant endsAt) {

    public Sale {
        Objects.requireNonNull(startsAt, "startsAt");
        Objects.requireNonNull(endsAt, "endsAt");
        if (price <= 0) {
            throw new IllegalArgumentException("price must be > 0");
        }
        if (!startsAt.isBefore(endsAt)) {
            throw new IllegalArgumentException("startsAt must be before endsAt");
        }
    }

    public boolean isActiveAt(Instant at) {
        return !at.isBefore(startsAt) && at.isBefore(endsAt);
    }
}
