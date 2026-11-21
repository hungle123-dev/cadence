package com.cadence.catalog.infrastructure.restapi.uidto;

import java.time.Instant;
import java.util.List;

import com.cadence.catalog.application.dto.SitemapView;

import io.swagger.v3.oas.annotations.media.Schema;

/** Input for /sitemap.xml (N4, AC 4.6): every category, visible products only. The web app builds the URLs. */
@Schema(name = "Sitemap")
public record SitemapResponse(List<SitemapEntry> categories, List<SitemapEntry> products) {

    public static SitemapResponse from(SitemapView view) {
        return new SitemapResponse(
                view.categories().stream().map(SitemapEntry::from).toList(),
                view.products().stream().map(SitemapEntry::from).toList());
    }

    @Schema(name = "SitemapEntry")
    public record SitemapEntry(
            @Schema(example = "tempo-2") String slug,
            @Schema(description = "UTC, becomes <lastmod>") Instant updatedAt) {

        static SitemapEntry from(SitemapView.Entry entry) {
            return new SitemapEntry(entry.slug(), entry.updatedAt());
        }
    }
}
