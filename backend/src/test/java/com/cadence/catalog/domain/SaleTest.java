package com.cadence.catalog.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;

import org.junit.jupiter.api.Test;

import com.cadence.catalog.domain.model.Sale;

/** R-PRICE, sale window [start, end). Plain JUnit: no Spring, no database. */
class SaleTest {

    private static final Instant START = Instant.parse("2026-12-01T00:00:00Z");
    private static final Instant END = Instant.parse("2026-12-08T00:00:00Z");

    // ---------- Active window ----------

    @Test
    void is_active_exactly_at_its_start() {
        Sale sale = new Sale(990_000, START, END);

        assertThat(sale.isActiveAt(START)).isTrue();
    }

    @Test
    void is_not_active_exactly_at_its_end() {
        Sale sale = new Sale(990_000, START, END);

        assertThat(sale.isActiveAt(END)).isFalse();
    }

    @Test
    void is_not_active_before_it_starts() {
        Sale sale = new Sale(990_000, START, END);

        assertThat(sale.isActiveAt(START.minusSeconds(1))).isFalse();
    }

    @Test
    void is_active_in_the_middle() {
        Sale sale = new Sale(990_000, START, END);

        assertThat(sale.isActiveAt(Instant.parse("2026-12-04T12:00:00Z"))).isTrue();
    }

    // ---------- Invariants: an invalid sale cannot be created ----------

    @Test
    void rejects_start_after_end() {
        assertThatThrownBy(() -> new Sale(990_000, END, START))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejects_start_equal_to_end() {
        assertThatThrownBy(() -> new Sale(990_000, START, START))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejects_non_positive_price() {
        assertThatThrownBy(() -> new Sale(0, START, END))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejects_missing_dates() {
        assertThatThrownBy(() -> new Sale(990_000, null, END))
                .isInstanceOf(NullPointerException.class);
    }
}
