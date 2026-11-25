package com.cadence.catalog.domain.valueobject;

import java.time.Instant;

import com.cadence.common.domain.util.Preconditions;

/**
 * A sale price with its time window (BR-PRC-09): active in [startsAt, endsAt), that is from the
 * start moment included up to the end moment excluded.
 *
 * @param endsAt null when the sale has no end
 */
public record Sale(long price, Instant startsAt, Instant endsAt) {

    public Sale {
        Preconditions.requireNonNull(startsAt, "startsAt");
        if (price <= 0) {
            throw new IllegalArgumentException("price must be > 0");
        }
        if (endsAt != null && !startsAt.isBefore(endsAt)) {
            throw new IllegalArgumentException("startsAt must be before endsAt");
        }
    }

    public boolean isActiveAt(Instant at) {
        return !at.isBefore(startsAt) && (endsAt == null || at.isBefore(endsAt));
    }
}
