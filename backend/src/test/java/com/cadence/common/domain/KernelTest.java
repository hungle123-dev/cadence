package com.cadence.common.domain;

import com.cadence.common.application.PageResult;
import com.cadence.common.domain.error.ErrorCategory;
import com.cadence.common.domain.error.ErrorCode;
import com.cadence.common.domain.error.ResourceNotFoundException;
import com.cadence.common.domain.time.BusinessTime;
import com.cadence.common.domain.util.Preconditions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class KernelTest {

    @Test
    void requireNotBlank_rejectsBlankText() {
        assertThatThrownBy(() -> Preconditions.requireNotBlank("  ", "name"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("name");
    }

    @Test
    void pageResult_computesTotalPages() {
        assertThat(new PageResult<>(List.of("a", "b"), 1, 12, 25).totalPages()).isEqualTo(3);
        assertThat(new PageResult<>(List.of(), 1, 12, 0).totalPages()).isZero();
    }

    @Test
    void pageResult_rejectsPageZeroBecausePagesStartAtOne() {
        assertThatThrownBy(() -> new PageResult<>(List.of(), 0, 12, 0))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void notFoundException_cannotCarryACodeOfAnotherCategory() {
        ErrorCode validationCode = new ErrorCode() {
            public String getCode() { return "X_INVALID"; }
            public String getDefaultMessage() { return "invalid"; }
            public ErrorCategory getCategory() { return ErrorCategory.VALIDATION; }
        };

        assertThatThrownBy(() -> new ResourceNotFoundException(validationCode) { })
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("X_INVALID");
    }

    @Test
    @DisplayName("C-09 00:30 ngày 25/11 giờ Việt Nam (17:30 UTC ngày 24/11) là ngày 25/11")
    void businessTime_usesVietnamTime() {
        Instant justAfterMidnightInVietnam = Instant.parse("2026-11-24T17:30:00Z");

        assertThat(BusinessTime.dateOf(justAfterMidnightInVietnam)).isEqualTo(LocalDate.of(2026, 11, 25));
        assertThat(BusinessTime.timeOf(justAfterMidnightInVietnam)).isEqualTo(LocalTime.of(0, 30));
    }
}
