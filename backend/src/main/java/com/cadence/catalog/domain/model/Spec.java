package com.cadence.catalog.domain.model;

import com.cadence.common.domain.util.Preconditions;

/** One line of the PDP specification table, for example "Trọng lượng: 228 g". */
public record Spec(String name, String value) {

    public Spec {
        Preconditions.requireNotBlank(name, "name");
        Preconditions.requireNotBlank(value, "value");
    }
}
