package com.cadence.catalog.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;

import org.junit.jupiter.api.Test;

import com.cadence.catalog.domain.model.ColorwayPricing;
import com.cadence.catalog.domain.model.EffectivePrice;
import com.cadence.catalog.domain.model.Sale;

/**
 * R-PRICE: effective price of a colorway (AC 1.5, 1.6). Plain JUnit: no Spring, no database.
 * The exact start/end boundaries are covered by SaleTest, not repeated here.
 */
class ColorwayPricingTest {

    private static final Instant START = Instant.parse("2026-12-01T00:00:00Z");
    private static final Instant END = Instant.parse("2026-12-08T00:00:00Z");
    private static final Instant DURING = Instant.parse("2026-12-04T12:00:00Z");

    private static final long LIST_PRICE = 1_290_000;
    private static final long SALE_PRICE = 990_000;

    // ---------- Effective price ----------

    @Test
    void without_sale_pays_list_price() {
        ColorwayPricing pricing = new ColorwayPricing(LIST_PRICE, null);

        EffectivePrice price = pricing.effectivePriceAt(DURING);

        assertThat(price.amount()).isEqualTo(LIST_PRICE);
        assertThat(price.compareAt()).isNull();
        assertThat(price.onSale()).isFalse();
    }

    @Test
    void during_sale_pays_sale_price_and_shows_list_price_struck() {
        ColorwayPricing pricing = new ColorwayPricing(LIST_PRICE, new Sale(SALE_PRICE, START, END));

        EffectivePrice price = pricing.effectivePriceAt(DURING);

        assertThat(price.amount()).isEqualTo(SALE_PRICE);
        assertThat(price.compareAt()).isEqualTo(LIST_PRICE);
        assertThat(price.onSale()).isTrue();
    }

    @Test
    void before_sale_pays_list_price() {
        ColorwayPricing pricing = new ColorwayPricing(LIST_PRICE, new Sale(SALE_PRICE, START, END));

        EffectivePrice price = pricing.effectivePriceAt(START.minusSeconds(1));

        assertThat(price.amount()).isEqualTo(LIST_PRICE);
        assertThat(price.compareAt()).isNull();
    }

    @Test
    void after_sale_pays_list_price() {
        ColorwayPricing pricing = new ColorwayPricing(LIST_PRICE, new Sale(SALE_PRICE, START, END));

        EffectivePrice price = pricing.effectivePriceAt(END.plusSeconds(3600));

        assertThat(price.amount()).isEqualTo(LIST_PRICE);
        assertThat(price.compareAt()).isNull();
    }

    // ---------- Invariants: an invalid pricing cannot be created ----------

    @Test
    void rejects_non_positive_list_price() {
        assertThatThrownBy(() -> new ColorwayPricing(0, null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejects_sale_price_equal_to_list_price() {
        Sale sale = new Sale(LIST_PRICE, START, END);

        assertThatThrownBy(() -> new ColorwayPricing(LIST_PRICE, sale))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejects_sale_price_above_list_price() {
        Sale sale = new Sale(LIST_PRICE + 1, START, END);

        assertThatThrownBy(() -> new ColorwayPricing(LIST_PRICE, sale))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
