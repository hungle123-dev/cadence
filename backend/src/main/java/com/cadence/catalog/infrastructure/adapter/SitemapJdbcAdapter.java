package com.cadence.catalog.infrastructure.adapter;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import com.cadence.catalog.application.dto.SitemapView;
import com.cadence.catalog.application.port.driven.SitemapReadPort;

import lombok.RequiredArgsConstructor;

/**
 * Sitemap entries (NFR-SEO-04): every category, and the products on sale. A category's lastmod is
 * that of its freshest product on sale; null when it has none yet.
 */
@Component
@RequiredArgsConstructor
public class SitemapJdbcAdapter implements SitemapReadPort {

    /** The later of the last edit and the launch, so a scheduled launch shows as fresh. */
    private static final String UPDATED = "greatest(p.updated_at, p.launch_at)";

    private final JdbcClient jdbc;

    @Override
    @Transactional(readOnly = true, isolation = Isolation.REPEATABLE_READ)
    public SitemapView findSitemapEntries(Instant now) {
        OffsetDateTime at = OffsetDateTime.ofInstant(now, ZoneOffset.UTC);
        List<SitemapView.Entry> categories =
                jdbc.sql(
                                """
                                SELECT c.slug, max(%s) AS updated_at
                                FROM catalog.categories c
                                LEFT JOIN catalog.products p ON p.category_id = c.id AND %s
                                GROUP BY c.id, c.slug, c.sort_order
                                ORDER BY c.sort_order, c.slug
                                """
                                        .formatted(UPDATED, CatalogSql.ON_SALE))
                        .param("now", at)
                        .query((rs, rowNum) ->
                                new SitemapView.Entry(rs.getString("slug"), instant(rs)))
                        .list();
        List<SitemapView.Entry> products =
                jdbc.sql(
                                """
                                SELECT p.slug, %s AS updated_at
                                FROM catalog.products p
                                WHERE %s
                                ORDER BY p.slug
                                """
                                        .formatted(UPDATED, CatalogSql.ON_SALE))
                        .param("now", at)
                        .query((rs, rowNum) ->
                                new SitemapView.Entry(
                                        rs.getString("slug"),
                                        rs.getObject("updated_at", OffsetDateTime.class)
                                                .toInstant()))
                        .list();
        return new SitemapView(categories, products);
    }

    private static Instant instant(ResultSet rs) throws SQLException {
        OffsetDateTime value = rs.getObject("updated_at", OffsetDateTime.class);
        return value == null ? null : value.toInstant();
    }
}
