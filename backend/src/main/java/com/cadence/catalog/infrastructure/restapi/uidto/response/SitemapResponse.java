package com.cadence.catalog.infrastructure.restapi.uidto.response;

import java.time.Instant;
import java.util.List;

import com.cadence.catalog.application.dto.SitemapView;

import io.swagger.v3.oas.annotations.media.Schema;

/** What the web app turns into /sitemap.xml (NFR-SEO-04): slugs, not URLs. */
@Schema(name = "Sitemap")
public record SitemapResponse(List<EntryResponse> categories, List<EntryResponse> products) {

    public SitemapResponse {
        categories = List.copyOf(categories);
        products = List.copyOf(products);
    }

    public static SitemapResponse from(SitemapView sitemap) {
        return new SitemapResponse(entries(sitemap.categories()), entries(sitemap.products()));
    }

    private static List<EntryResponse> entries(List<SitemapView.Entry> entries) {
        return entries.stream()
                .map(entry -> new EntryResponse(entry.slug(), entry.updatedAt()))
                .toList();
    }

    @Schema(name = "SitemapEntry")
    public record EntryResponse(
            @Schema(example = "tempo-2") String slug,
            @Schema(description = "Becomes <lastmod>") Instant updatedAt) {}
}
