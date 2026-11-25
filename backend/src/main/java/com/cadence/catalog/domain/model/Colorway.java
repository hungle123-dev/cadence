package com.cadence.catalog.domain.model;

import java.util.List;
import java.util.UUID;

import com.cadence.common.domain.util.Preconditions;

/**
 * One colour of a product, with its photos and sizes. Every colorway costs the same as its product
 * (BR-PRC-08), so the price lives on {@link Product}.
 *
 * @param images in display order
 * @param variants in size order (39 before 40, S before M)
 */
public record Colorway(
        UUID id,
        String name,
        ColorFamily colorFamily,
        List<ProductImage> images,
        List<Variant> variants) {

    public Colorway {
        Preconditions.requireNonNull(id, "id");
        Preconditions.requireNotBlank(name, "name");
        Preconditions.requireNonNull(colorFamily, "colorFamily");
        images = List.copyOf(images);
        variants = List.copyOf(variants);
    }

    /** A colorway can be bought when at least one of its sizes can. */
    public boolean isInStock() {
        return variants.stream().anyMatch(Variant::available);
    }
}
