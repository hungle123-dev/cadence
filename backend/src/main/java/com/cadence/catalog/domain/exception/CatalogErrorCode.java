package com.cadence.catalog.domain.exception;

import com.cadence.common.domain.error.ErrorCategory;
import com.cadence.common.domain.error.ErrorCode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** Every error the catalog can return. The codes are part of the API contract. */
@Getter
@RequiredArgsConstructor
public enum CatalogErrorCode implements ErrorCode {
    /** Unknown category slug. */
    CATEGORY_NOT_FOUND(
            "CATALOG_CATEGORY_NOT_FOUND", "Không tìm thấy danh mục", ErrorCategory.NOT_FOUND),
    /** Unknown slug, or a draft or archived product (NFR-SEO-06); both look the same. */
    PRODUCT_NOT_FOUND(
            "CATALOG_PRODUCT_NOT_FOUND", "Không tìm thấy sản phẩm", ErrorCategory.NOT_FOUND),
    /** A list filter, sort or page value the API does not accept (FR-CAT-02, FR-CAT-03). */
    INVALID_FILTER("CATALOG_INVALID_FILTER", "Bộ lọc không hợp lệ", ErrorCategory.VALIDATION);

    private final String code;
    private final String defaultMessage;
    private final ErrorCategory category;
}
