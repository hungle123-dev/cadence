package com.cadence.catalog.infrastructure.restapi.uidto;

import java.time.Instant;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

/** Input for /sitemap.xml (N4, AC 4.6): visible products only. */
@Schema(name = "Sitemap")
public record SitemapResponse(List<SitemapEntry> categories, List<SitemapEntry> products) {

    @Schema(name = "SitemapEntry")
    public record SitemapEntry(
            @Schema(example = "tempo-2") String slug,
            @Schema(description = "UTC, becomes <lastmod>") Instant updatedAt) {
    }
}
