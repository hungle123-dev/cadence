package com.cadence.catalog.domain.valueobject;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PricingTest {

    private static final Instant TEN = Instant.parse("2026-11-25T03:00:00Z");
    private static final Instant NOON = Instant.parse("2026-11-25T05:00:00Z");

    @Test
    @DisplayName("TC-CAT-13 Khuyến mãi [10:00, 12:00): tính lúc bắt đầu, không tính lúc kết thúc")
    void priceAt_appliesTheSaleFromItsStartUntilJustBeforeItsEnd() {
        var pricing = new ProductPricing(2_890_000, new Sale(2_290_000, TEN, NOON));

        assertThat(pricing.priceAt(TEN.minusSeconds(1)))
                .isEqualTo(new EffectivePrice(2_890_000, null));
        assertThat(pricing.priceAt(TEN)).isEqualTo(new EffectivePrice(2_290_000, 2_890_000L));
        assertThat(pricing.priceAt(NOON.minusSeconds(1)).amount()).isEqualTo(2_290_000);
        assertThat(pricing.priceAt(NOON)).isEqualTo(new EffectivePrice(2_890_000, null));
    }

    @Test
    @DisplayName("TC-CAT-13 Khuyến mãi không có thời điểm kết thúc thì còn hiệu lực mãi")
    void priceAt_keepsASaleWithoutEndForever() {
        var pricing = new ProductPricing(990_000, new Sale(790_000, TEN, null));

        assertThat(pricing.priceAt(Instant.parse("2030-01-01T00:00:00Z")).amount())
                .isEqualTo(790_000);
    }

    @Test
    @DisplayName("TC-CAT-13 Giá khuyến mãi ≥ giá gốc, kết thúc ≤ bắt đầu bị từ chối")
    void constructors_rejectAnImpossibleSale() {
        assertThatThrownBy(() -> new ProductPricing(1_000_000, new Sale(1_000_000, TEN, null)))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Sale(500_000, NOON, TEN))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Sale(500_000, TEN, TEN))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Sale(0, TEN, null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new ProductPricing(0, null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("TC-CAT-16 Nhãn −N% làm tròn xuống, 0 khi không có khuyến mãi")
    void discountPercent_isRoundedDown() {
        assertThat(new EffectivePrice(2_290_000, 2_890_000L).discountPercent()).isEqualTo(20);
        assertThat(new EffectivePrice(790_000, 990_000L).discountPercent()).isEqualTo(20);
        assertThat(new EffectivePrice(999, 1_000L).discountPercent()).isZero();
        assertThat(new EffectivePrice(990_000, null).discountPercent()).isZero();
        assertThat(new EffectivePrice(990_000, null).onSale()).isFalse();
    }
}
