package com.cadence.catalog.infrastructure.adapter;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.transaction.annotation.Transactional;

import com.cadence.catalog.application.dto.SitemapView;
import com.cadence.catalog.application.port.driven.SitemapReadPort;

/** Slugs for /sitemap.xml with native SQL, like the PLP: visibility uses the shared CatalogSql.VISIBLE. */
public class SitemapReadAdapter implements SitemapReadPort {

    /** Categories are always on the menu, so all of them, even empty ones (Q7, AC 1.10). */
    private static final String CATEGORIES = "SELECT slug, updated_at FROM catalog.categories ORDER BY slug";

    /** lastmod is the later of the last edit and the publish time: a scheduled launch looks fresh (Q6). */
    private static final String PRODUCTS = """
            SELECT p.slug, GREATEST(p.updated_at, p.publish_at) AS updated_at
            FROM catalog.products p
            WHERE %s
            ORDER BY p.slug
            """.formatted(CatalogSql.VISIBLE);

    private final JdbcClient jdbc;

    public SitemapReadAdapter(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    @Transactional(readOnly = true)
    public SitemapView find(Instant now) {
        var categories = jdbc.sql(CATEGORIES)
                .query((rs, rowNum) -> toEntry(rs))
                .list();
        var products = jdbc.sql(PRODUCTS)
                .param("now", OffsetDateTime.ofInstant(now, ZoneOffset.UTC))
                .query((rs, rowNum) -> toEntry(rs))
                .list();
        return new SitemapView(categories, products);
    }

    private static SitemapView.Entry toEntry(ResultSet rs) throws SQLException {
        return new SitemapView.Entry(rs.getString("slug"),
                rs.getObject("updated_at", OffsetDateTime.class).toInstant());
    }
}
