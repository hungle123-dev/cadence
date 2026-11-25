package com.cadence.catalog.application.dto;

/**
 * List order (FR-CAT-03). Every order ends with the product id, so equal keys never swap products
 * between pages and "Xem thêm" never repeats or skips one.
 */
public enum ProductSort {
    /** Featured rank ascending, products without a rank last, then newest first. */
    FEATURED,
    /** Launch time, newest first. */
    NEWEST,
    PRICE_ASC,
    PRICE_DESC
}
