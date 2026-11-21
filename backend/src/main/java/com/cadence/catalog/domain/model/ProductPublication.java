package com.cadence.catalog.domain.model;

import java.time.Instant;
import java.util.Objects;

/** R-VIS: whether customers can see a product at a given moment. publishAt may be null for DRAFT and ARCHIVED. */
public record ProductPublication(ProductStatus status, Instant publishAt) {

    public ProductPublication {
        Objects.requireNonNull(status, "status");
        if ((status == ProductStatus.SCHEDULED || status == ProductStatus.PUBLISHED) && publishAt == null) {
            throw new IllegalArgumentException("publishAt must not be null when status is SCHEDULED or PUBLISHED");
        }
    }

    public boolean isVisibleAt(Instant at) {
        return switch (status) {
            case DRAFT -> false;
            case ARCHIVED -> false;
            case PUBLISHED -> true;
            case SCHEDULED -> !at.isBefore(publishAt);
        };
    }
}
