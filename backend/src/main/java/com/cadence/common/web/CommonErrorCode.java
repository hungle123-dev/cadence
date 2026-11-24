package com.cadence.common.web;

import com.cadence.common.domain.error.ErrorCategory;
import com.cadence.common.domain.error.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** Errors that do not belong to any business module. */
@Getter
@RequiredArgsConstructor
public enum CommonErrorCode implements ErrorCode {
    VALIDATION_ERROR("VALIDATION_ERROR", "Dữ liệu gửi lên chưa hợp lệ.", ErrorCategory.VALIDATION),
    MALFORMED_REQUEST("MALFORMED_REQUEST", "Yêu cầu không đọc được.", ErrorCategory.VALIDATION),
    CONCURRENT_UPDATE("CONCURRENT_UPDATE", "Dữ liệu vừa được người khác thay đổi. Vui lòng tải lại và thử lại.", ErrorCategory.CONFLICT),
    NOT_FOUND("NOT_FOUND", "Không tìm thấy.", ErrorCategory.NOT_FOUND),
    INTERNAL_ERROR("INTERNAL_ERROR", "Đã có lỗi xảy ra. Vui lòng thử lại sau.", ErrorCategory.INTERNAL);

    private final String code;
    private final String defaultMessage;
    private final ErrorCategory category;
}
