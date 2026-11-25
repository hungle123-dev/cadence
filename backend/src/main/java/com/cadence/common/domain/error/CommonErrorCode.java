package com.cadence.common.domain.error;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Technical codes shared by every module. Business codes belong to their module's own enum.
 *
 * <p>Errors Spring MVC raises itself (unknown path, wrong method) are not listed: their code is
 * derived from the HTTP status ({@code NOT_FOUND}, {@code METHOD_NOT_ALLOWED}).
 */
@Getter
@RequiredArgsConstructor
public enum CommonErrorCode implements ErrorCode {
    /** Bean Validation or type conversion failed on the request; invalidParams says where. */
    VALIDATION_ERROR("VALIDATION_ERROR", "Dữ liệu gửi lên không hợp lệ", ErrorCategory.VALIDATION),
    /** Optimistic lock (@Version): someone else saved the same record first. */
    CONCURRENT_UPDATE(
            "CONCURRENT_UPDATE",
            "Dữ liệu vừa được người khác thay đổi. Vui lòng tải lại và thử lại.",
            ErrorCategory.CONFLICT),
    INTERNAL_ERROR(
            "INTERNAL_ERROR", "Đã có lỗi xảy ra. Vui lòng thử lại sau.", ErrorCategory.INTERNAL);

    private final String code;
    private final String defaultMessage;
    private final ErrorCategory category;
}
