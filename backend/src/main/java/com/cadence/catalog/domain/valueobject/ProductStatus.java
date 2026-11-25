package com.cadence.catalog.domain.valueobject;

/**
 * Stored lifecycle of a product (SRS 6.5). "Đang bán" and "sắp mở bán" are not statuses: {@link
 * ProductPublication} derives them from the launch time, so a launch needs no background job.
 */
public enum ProductStatus {
    DRAFT,
    PUBLISHED,
    ARCHIVED
}
