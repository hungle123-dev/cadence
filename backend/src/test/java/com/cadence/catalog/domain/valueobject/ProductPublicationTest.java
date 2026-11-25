package com.cadence.catalog.domain.valueobject;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Duration;
import java.time.Instant;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ProductPublicationTest {

    private static final Instant LAUNCH = Instant.parse("2026-11-25T13:00:00Z");

    @Test
    @DisplayName("TC-CAT-06 Trước giờ mở bán 1 giây là sắp mở bán, đúng giờ là đang bán")
    void visibilityAt_switchesToOnSaleExactlyAtLaunch() {
        var publication = new ProductPublication(ProductStatus.PUBLISHED, LAUNCH);

        assertThat(publication.visibilityAt(LAUNCH.minusSeconds(1)))
                .isEqualTo(Visibility.COMING_SOON);
        assertThat(publication.visibilityAt(LAUNCH)).isEqualTo(Visibility.ON_SALE);
    }

    @Test
    @DisplayName("TC-CAT-14 Nháp và ngừng bán luôn ẩn, kể cả sau giờ mở bán")
    void visibilityAt_hidesDraftsAndArchivedProducts() {
        assertThat(new ProductPublication(ProductStatus.DRAFT, null).visibilityAt(LAUNCH))
                .isEqualTo(Visibility.HIDDEN);
        assertThat(
                        new ProductPublication(ProductStatus.ARCHIVED, LAUNCH)
                                .visibilityAt(LAUNCH.plusSeconds(60)))
                .isEqualTo(Visibility.HIDDEN);
    }

    @Test
    @DisplayName("Mục 6.5 Sản phẩm đã đăng bán phải có giờ mở bán")
    void constructor_requiresALaunchTimeOncePublished() {
        assertThatThrownBy(() -> new ProductPublication(ProductStatus.PUBLISHED, null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new ProductPublication(ProductStatus.ARCHIVED, null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("TC-CAT-16 Nhãn Mới trong 30 ngày kể từ giờ mở bán, mất ở ngày thứ 31")
    void isNewAt_lastsThirtyDaysFromLaunch() {
        var publication = new ProductPublication(ProductStatus.PUBLISHED, LAUNCH);
        Instant thirtyDaysLater = LAUNCH.plus(Duration.ofDays(30));

        assertThat(publication.isNewAt(LAUNCH.minusSeconds(1))).isFalse(); // not launched yet
        assertThat(publication.isNewAt(LAUNCH)).isTrue();
        assertThat(publication.isNewAt(thirtyDaysLater.minusSeconds(1))).isTrue();
        assertThat(publication.isNewAt(thirtyDaysLater)).isFalse();
    }
}
