package com.cadence.catalog.application.dto;

import java.time.Instant;
import java.util.List;

/**
 * What /sitemap.xml lists (NFR-SEO-04): every category, and the products on sale. Coming soon,
 * draft and archived products are left out.
 */
public record SitemapView(List<Entry> categories, List<Entry> products) {

    public SitemapView {
        categories = List.copyOf(categories);
        products = List.copyOf(products);
    }

    /** updatedAt becomes {@code <lastmod>}. */
    public record Entry(String slug, Instant updatedAt) {}
}
