package com.cadence.catalog.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;

import org.junit.jupiter.api.Test;

import com.cadence.catalog.domain.model.ProductPublication;
import com.cadence.catalog.domain.model.ProductStatus;

/** R-VIS: which products customers can see (AC 1.3, 1.4, 3.8). Plain JUnit: no Spring, no database. */
class ProductPublicationTest {

    private static final Instant PUBLISH_AT = Instant.parse("2026-12-01T09:00:00Z");
    private static final Instant LATER = PUBLISH_AT.plusSeconds(3600);

    // ---------- Visibility by status ----------

    @Test
    void draft_is_hidden() {
        ProductPublication publication = new ProductPublication(ProductStatus.DRAFT, null);

        assertThat(publication.isVisibleAt(LATER)).isFalse();
    }

    @Test
    void archived_is_hidden() {
        ProductPublication publication = new ProductPublication(ProductStatus.ARCHIVED, PUBLISH_AT);

        assertThat(publication.isVisibleAt(LATER)).isFalse();
    }

    @Test
    void published_is_visible() {
        ProductPublication publication = new ProductPublication(ProductStatus.PUBLISHED, PUBLISH_AT);

        assertThat(publication.isVisibleAt(LATER)).isTrue();
    }

    // ---------- Scheduled launch ----------

    @Test
    void scheduled_is_hidden_before_publish_time() {
        ProductPublication publication = new ProductPublication(ProductStatus.SCHEDULED, PUBLISH_AT);

        assertThat(publication.isVisibleAt(PUBLISH_AT.minusSeconds(1))).isFalse();
    }

    @Test
    void scheduled_is_visible_exactly_at_publish_time() {
        ProductPublication publication = new ProductPublication(ProductStatus.SCHEDULED, PUBLISH_AT);

        assertThat(publication.isVisibleAt(PUBLISH_AT)).isTrue();
    }

    @Test
    void scheduled_is_visible_after_publish_time() {
        ProductPublication publication = new ProductPublication(ProductStatus.SCHEDULED, PUBLISH_AT);

        assertThat(publication.isVisibleAt(PUBLISH_AT.plusSeconds(60))).isTrue();
    }

    // ---------- Invariants ----------

    @Test
    void scheduled_without_publish_time_is_rejected() {
        assertThatThrownBy(() -> new ProductPublication(ProductStatus.SCHEDULED, null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void published_without_publish_time_is_rejected() {
        assertThatThrownBy(() -> new ProductPublication(ProductStatus.PUBLISHED, null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void draft_without_publish_time_is_allowed() {
        assertThatNoException().isThrownBy(() -> new ProductPublication(ProductStatus.DRAFT, null));
    }

    @Test
    void status_is_required() {
        assertThatThrownBy(() -> new ProductPublication(null, PUBLISH_AT))
                .isInstanceOf(NullPointerException.class);
    }
}
