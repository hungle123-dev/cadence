package com.cadence.catalog.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import com.cadence.TestcontainersConfiguration;
import com.cadence.catalog.application.dto.SitemapView;
import com.cadence.catalog.application.port.driven.SitemapReadPort;

/** SitemapReadAdapter on PostgreSQL with sql/plp-tiles.sql (N4, AC 4.6). @Transactional rolls the data back. */
@SpringBootTest
@Import(TestcontainersConfiguration.class)
@Transactional
@Sql({ "classpath:db/seed/R__seed_categories.sql", "classpath:sql/plp-tiles.sql" })
class SitemapReadAdapterIntegrationTest {

    private static final Instant NOW = Instant.parse("2026-12-04T12:00:00Z");

    @Autowired
    SitemapReadPort port;

    @Autowired
    JdbcClient jdbc;

    @Test
    void only_visible_products_in_slug_order() {
        var slugs = port.find(NOW).products().stream().map(SitemapView.Entry::slug).toList();

        // draft-x (draft) and sched-future (tomorrow) stay out; sched-past went live a minute ago
        assertThat(slugs).containsExactly("bulk", "run-a", "run-b", "run-c", "sched-past");
    }

    @Test
    void every_category_even_empty_ones() {
        var slugs = port.find(NOW).categories().stream().map(SitemapView.Entry::slug).toList();

        assertThat(slugs).containsExactly("ao-chay-bo", "ao-thun", "giay", "giay-chay-bo", "giay-lifestyle", "quan-ao");
    }

    @Test
    void a_scheduled_launch_is_as_fresh_as_its_publish_time() {
        // last edited weeks before it went live
        jdbc.sql("UPDATE catalog.products SET updated_at = :at WHERE slug = 'sched-past'")
                .param("at", OffsetDateTime.of(2026, 11, 1, 0, 0, 0, 0, ZoneOffset.UTC))
                .update();

        var schedPast = port.find(NOW).products().stream()
                .filter(e -> e.slug().equals("sched-past"))
                .findFirst()
                .orElseThrow();

        assertThat(schedPast.updatedAt()).isEqualTo(Instant.parse("2026-12-04T11:59:00Z"));
    }

    @Test
    void an_edit_after_launch_wins() {
        jdbc.sql("UPDATE catalog.products SET updated_at = :at WHERE slug = 'run-a'")
                .param("at", OffsetDateTime.of(2026, 12, 2, 8, 0, 0, 0, ZoneOffset.UTC))
                .update();

        var runA = port.find(NOW).products().stream()
                .filter(e -> e.slug().equals("run-a"))
                .findFirst()
                .orElseThrow();

        assertThat(runA.updatedAt()).isEqualTo(Instant.parse("2026-12-02T08:00:00Z"));
    }
}
