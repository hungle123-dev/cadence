package com.cadence.catalog.application.dto;

import java.time.Instant;
import java.util.List;

import org.jspecify.annotations.Nullable;

/**
 * What /sitemap.xml lists (NFR-SEO-04): every category, and the products on sale. Coming soon,
 * draft and archived products are left out.
 */
public record SitemapView(List<Entry> categories, List<Entry> products) {

    public SitemapView {
        categories = List.copyOf(categories);
        products = List.copyOf(products);
    }

    /**
     * @param updatedAt becomes {@code <lastmod>}; null for a category with no product on sale yet
     */
    public record Entry(String slug, @Nullable Instant updatedAt) {}
}
