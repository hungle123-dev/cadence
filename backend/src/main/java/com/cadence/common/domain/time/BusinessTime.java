package com.cadence.common.domain.time;

import com.cadence.common.domain.util.Preconditions;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

/**
 * Business dates and times are in Vietnam time (SRS C-09): order-number date, the 14:00 same-day cut-off,
 * launch times, revenue per day. Storage stays UTC ({@link Instant}); convert only through this class.
 */
public final class BusinessTime {

    public static final ZoneId ZONE = ZoneId.of("Asia/Ho_Chi_Minh");

    private BusinessTime() {
    }

    public static LocalDate dateOf(Instant instant) {
        return Preconditions.requireNonNull(instant, "instant").atZone(ZONE).toLocalDate();
    }

    public static LocalTime timeOf(Instant instant) {
        return Preconditions.requireNonNull(instant, "instant").atZone(ZONE).toLocalTime();
    }

    public static ZonedDateTime zoned(Instant instant) {
        return Preconditions.requireNonNull(instant, "instant").atZone(ZONE);
    }
}
