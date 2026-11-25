package com.cadence.catalog.infrastructure.config;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * Catalog settings ({@code cadence.catalog.*}).
 *
 * @param priceBandBoundaries where one PLP price band ends and the next starts, in VND (FR-CAT-02,
 *     D-31). The default gives under 1M, 1–2M, 2–3M and 3M or more.
 * @param imageUrlTemplate the image source (BR-CAT-01): {@code {key}} is replaced by the image key.
 *     Changing the source only changes this value (SRS A-04).
 */
@ConfigurationProperties("cadence.catalog")
public record CatalogProperties(
        @DefaultValue({"1000000", "2000000", "3000000"}) List<Long> priceBandBoundaries,
        @DefaultValue("https://images.unsplash.com/{key}?auto=format&fit=crop&q=80&w=1600&h=1600")
                String imageUrlTemplate) {

    public CatalogProperties {
        priceBandBoundaries = List.copyOf(priceBandBoundaries);
        // Fail at startup with the setting's name, not later with "max must be above min".
        long previous = 0;
        for (long boundary : priceBandBoundaries) {
            if (boundary <= previous) {
                throw new IllegalArgumentException(
                        "cadence.catalog.price-band-boundaries must be positive and strictly"
                                + " ascending, got "
                                + priceBandBoundaries);
            }
            previous = boundary;
        }
        if (!imageUrlTemplate.contains("{key}")) {
            throw new IllegalArgumentException(
                    "cadence.catalog.image-url-template must contain {key}");
        }
    }
}
