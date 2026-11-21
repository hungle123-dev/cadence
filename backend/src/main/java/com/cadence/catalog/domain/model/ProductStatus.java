package com.cadence.catalog.domain.model;

/** Lifecycle of a product (products.status, CAT-06). Visibility rule R-VIS: see ProductPublication. */
public enum ProductStatus {
    DRAFT, SCHEDULED, PUBLISHED, ARCHIVED
}
