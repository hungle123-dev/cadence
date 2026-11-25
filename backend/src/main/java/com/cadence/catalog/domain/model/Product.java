package com.cadence.catalog.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.cadence.catalog.domain.valueobject.EffectivePrice;
import com.cadence.catalog.domain.valueobject.ProductPricing;
import com.cadence.catalog.domain.valueobject.ProductPublication;
import com.cadence.catalog.domain.valueobject.Visibility;
import com.cadence.common.domain.util.Preconditions;

/**
 * Aggregate root of the catalog: one product with its colorways. The rules customers see live here
 * and in its value objects, not in services or SQL: visibility (R-VIS, SRS 6.5), price (R-PRICE,
 * BR-PRC-01), stock (R-STOCK), labels (FR-CAT-09) and the default SEO text (NFR-SEO-03).
 *
 * <p>Immutable for now: the storefront only reads products. Admin editing (FR-ADM-PRD) adds
 * behaviour methods that return a new state.
 *
 * @param featuredRank null when the product is not featured (FR-CAT-03)
 * @param rating sample data (SRS A-03); null when there is none
 * @param seoTitle admin override (FR-ADM-PRD-05); null means the default of {@link #seoTitle}
 * @param seoDescription admin override; null means the default of {@link #seoDescription()}
 * @param colorways in display order; the first is the default colorway
 */
public record Product(
        UUID id,
        String slug,
        String name,
        UUID categoryId,
        Gender gender,
        Sport sport,
        String description,
        List<Spec> specs,
        Fit fit,
        Integer featuredRank,
        BigDecimal rating,
        int reviewCount,
        String seoTitle,
        String seoDescription,
        ProductPricing pricing,
        ProductPublication publication,
        List<Colorway> colorways) {

    /** NFR-SEO-03: search engines show about this many characters of a description. */
    public static final int SEO_DESCRIPTION_LENGTH = 155;

    public Product {
        Preconditions.requireNonNull(id, "id");
        Preconditions.requireNotBlank(slug, "slug");
        Preconditions.requireNotBlank(name, "name");
        Preconditions.requireNonNull(categoryId, "categoryId");
        Preconditions.requireNonNull(gender, "gender");
        Preconditions.requireNonNull(sport, "sport");
        Preconditions.requireNonNull(description, "description");
        Preconditions.requireNonNull(fit, "fit");
        Preconditions.requireNonNull(pricing, "pricing");
        Preconditions.requireNonNull(publication, "publication");
        specs = List.copyOf(specs);
        colorways = List.copyOf(colorways);
    }

    /** R-VIS: on sale, coming soon, or hidden (which looks exactly like missing). */
    public Visibility visibilityAt(Instant now) {
        return publication.visibilityAt(now);
    }

    /** R-PRICE: the same for every colorway and size (BR-PRC-08). */
    public EffectivePrice priceAt(Instant now) {
        return pricing.priceAt(now);
    }

    /** R-STOCK: at least one size of one colorway can be bought ("Hết hàng" otherwise). */
    public boolean isInStock() {
        return colorways.stream().anyMatch(Colorway::isInStock);
    }

    /** FR-CAT-09 "Mới". */
    public boolean isNewAt(Instant now) {
        return publication.isNewAt(now);
    }

    /** NFR-SEO-03: the override, or "{name} – {category} | Cadence". */
    public String seoTitle(String categoryName) {
        if (seoTitle != null && !seoTitle.isBlank()) {
            return seoTitle;
        }
        return "%s – %s | Cadence".formatted(name, categoryName);
    }

    /**
     * NFR-SEO-03: the override, or the start of the description cut at a word boundary. A product
     * without a description falls back to its name.
     */
    public String seoDescription() {
        if (seoDescription != null && !seoDescription.isBlank()) {
            return seoDescription;
        }
        String text = description.strip().replaceAll("\\s+", " ");
        if (text.isEmpty()) {
            return name;
        }
        if (text.length() <= SEO_DESCRIPTION_LENGTH) {
            return text;
        }
        int cut = text.lastIndexOf(' ', SEO_DESCRIPTION_LENGTH);
        return text.substring(0, cut > 0 ? cut : SEO_DESCRIPTION_LENGTH);
    }
}
