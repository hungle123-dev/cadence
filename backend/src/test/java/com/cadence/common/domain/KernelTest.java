package com.cadence.common.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.cadence.common.application.PageResult;
import com.cadence.common.domain.error.ErrorCategory;
import com.cadence.common.domain.error.ErrorCode;
import com.cadence.common.domain.error.InvalidParam;
import com.cadence.common.domain.error.ResourceConflictException;
import com.cadence.common.domain.error.ResourceNotFoundException;
import com.cadence.common.domain.error.ValidationException;
import com.cadence.common.domain.time.BusinessTime;
import com.cadence.common.domain.util.Preconditions;

class KernelTest {

    private static final ErrorCode VALIDATION_CODE = code("X_INVALID", ErrorCategory.VALIDATION);

    @Test
    void requireNotBlank_rejectsBlankText() {
        assertThatThrownBy(() -> Preconditions.requireNotBlank("  ", "name"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("name");
    }

    @Test
    void requireNonNull_namesTheField() {
        assertThatThrownBy(() -> Preconditions.requireNonNull(null, "slug"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("slug must not be null");
    }

    @Test
    void pageResult_computesTotalPages() {
        assertThat(PageResult.of(List.of("a", "b"), 1, 12, 25).totalPages()).isEqualTo(3);
        assertThat(PageResult.of(List.of("a"), 1, 12, 12).totalPages()).isEqualTo(1);
        assertThat(PageResult.of(List.of(), 1, 12, 0).totalPages()).isZero();
    }

    @Test
    void pageResult_rejectsPageZeroBecausePagesStartAtOne() {
        assertThatThrownBy(() -> PageResult.of(List.of(), 0, 12, 0))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> PageResult.of(List.of(), 1, 0, 0))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> PageResult.of(List.of(), 1, 12, -1))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void pageResult_mapKeepsThePagingAndCopiesTheContent() {
        List<Integer> source = new ArrayList<>(List.of(1, 2));
        PageResult<Integer> page = PageResult.of(source, 2, 2, 5);
        source.add(3);

        PageResult<String> mapped = page.map(String::valueOf);

        assertThat(page.content()).containsExactly(1, 2);
        assertThat(mapped).isEqualTo(PageResult.of(List.of("1", "2"), 2, 2, 5));
    }

    @Test
    void notFoundException_cannotCarryACodeOfAnotherCategory() {
        assertThatThrownBy(
                        () -> {
                            throw new TestNotFoundException(VALIDATION_CODE);
                        })
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("X_INVALID");
    }

    @Test
    void conflictException_acceptsOnlyConflictCodes() {
        ErrorCode conflict = code("X_CONFLICT", ErrorCategory.CONFLICT);

        assertThat(new TestConflictException(conflict).getErrorCode()).isEqualTo(conflict);
        assertThatThrownBy(
                        () -> {
                            throw new TestConflictException(VALIDATION_CODE);
                        })
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("expected CONFLICT");
    }

    @Test
    void validationException_keepsAnImmutableCopyOfTheInvalidParams() {
        List<InvalidParam> params = new ArrayList<>(List.of(new InvalidParam("page", "phải ≥ 1")));
        ValidationException ex = new TestValidationException(VALIDATION_CODE, params);
        params.clear();

        assertThat(ex.getInvalidParams()).containsExactly(new InvalidParam("page", "phải ≥ 1"));
        assertThat(ex.getMessage()).isEqualTo("X_INVALID message");
        assertThatThrownBy(() -> ex.getInvalidParams().add(new InvalidParam("x", "y")))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    @DisplayName("C-09 00:30 ngày 25/11 giờ Việt Nam (17:30 UTC ngày 24/11) là ngày 25/11")
    void businessTime_usesVietnamTime() {
        Instant justAfterMidnightInVietnam = Instant.parse("2026-11-24T17:30:00Z");

        assertThat(BusinessTime.dateOf(justAfterMidnightInVietnam))
                .isEqualTo(LocalDate.of(2026, 11, 25));
        assertThat(BusinessTime.timeOf(justAfterMidnightInVietnam)).isEqualTo(LocalTime.of(0, 30));
        assertThat(BusinessTime.zoned(justAfterMidnightInVietnam).getOffset().getTotalSeconds())
                .isEqualTo(7 * 3600);
    }

    private static ErrorCode code(String code, ErrorCategory category) {
        return new TestErrorCode(code, code + " message", category);
    }

    private record TestErrorCode(
            String getCode, String getDefaultMessage, ErrorCategory getCategory)
            implements ErrorCode {}

    private static final class TestNotFoundException extends ResourceNotFoundException {
        TestNotFoundException(ErrorCode errorCode) {
            super(errorCode);
        }
    }

    private static final class TestConflictException extends ResourceConflictException {
        TestConflictException(ErrorCode errorCode) {
            super(errorCode);
        }
    }

    private static final class TestValidationException extends ValidationException {
        TestValidationException(ErrorCode errorCode, List<InvalidParam> params) {
            super(errorCode, params);
        }
    }
}
