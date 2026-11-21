package com.cadence.catalog.application.dto;

import java.time.Instant;
import java.util.List;

/** What ProductQueryUseCase.sitemap returns (N4, AC 4.6): every category and the visible products, by slug. */
public record SitemapView(List<Entry> categories, List<Entry> products) {

    /** updatedAt becomes <lastmod> in /sitemap.xml. */
    public record Entry(String slug, Instant updatedAt) {
    }
}
