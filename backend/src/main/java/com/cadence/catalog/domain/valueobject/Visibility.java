package com.cadence.catalog.domain.valueobject;

/** How a product shows to customers at one moment (SRS 6.5, the "cách hiển thị" table). */
public enum Visibility {
    /** In the PLP and sitemap; PDP indexed; can be bought. */
    ON_SALE,
    /** Only its PDP, with a countdown and noindex; cannot be bought (FR-CAT-07). */
    COMING_SOON,
    /** Draft or archived: answers exactly like a product that does not exist (NFR-SEO-06). */
    HIDDEN
}
