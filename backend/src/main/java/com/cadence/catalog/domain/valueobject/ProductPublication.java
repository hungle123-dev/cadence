package com.cadence.catalog.domain.valueobject;

import java.time.Duration;
import java.time.Instant;

import com.cadence.common.domain.util.Preconditions;

/**
 * Status plus launch time, and the rules that read them (SRS 6.5, FR-CAT-07, FR-CAT-09).
 *
 * @param launchAt null only for a draft; for a product published without a schedule it is the
 *     moment it was published
 */
public record ProductPublication(ProductStatus status, Instant launchAt) {

    /** FR-CAT-09: "Mới" while the launch is less than 30 days old. */
    public static final Duration NEW_FOR = Duration.ofDays(30);

    public ProductPublication {
        Preconditions.requireNonNull(status, "status");
        if (status != ProductStatus.DRAFT && launchAt == null) {
            throw new IllegalArgumentException("launchAt must not be null once published");
        }
    }

    /** R-VIS. The launch moment itself already counts as on sale. */
    public Visibility visibilityAt(Instant now) {
        if (status != ProductStatus.PUBLISHED) {
            return Visibility.HIDDEN;
        }
        return now.isBefore(launchAt) ? Visibility.COMING_SOON : Visibility.ON_SALE;
    }

    /** FR-CAT-09: launched, and less than {@link #NEW_FOR} ago. */
    public boolean isNewAt(Instant now) {
        return visibilityAt(now) == Visibility.ON_SALE && now.isBefore(launchAt.plus(NEW_FOR));
    }
}
