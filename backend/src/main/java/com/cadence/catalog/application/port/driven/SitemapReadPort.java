package com.cadence.catalog.application.port.driven;

import java.time.Instant;

import com.cadence.catalog.application.dto.SitemapView;

public interface SitemapReadPort {

    /**
     * Every category, and the products on sale at {@code now} (R-VIS in SQL, like the PLP). A
     * product's updatedAt is the later of its last edit and its launch, so a launch shows as fresh.
     */
    SitemapView findSitemapEntries(Instant now);
}
