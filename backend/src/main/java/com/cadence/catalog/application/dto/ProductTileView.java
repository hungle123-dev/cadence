package com.cadence.catalog.application.dto;

import org.jspecify.annotations.Nullable;

import com.cadence.catalog.domain.model.ProductImage;
import com.cadence.catalog.domain.valueobject.EffectivePrice;

/**
 * One product in a list (FR-CAT-01), worked out for one moment: price (R-PRICE), stock (R-STOCK)
 * and "Mới" (FR-CAT-09). The read port builds it, using the domain rules for every displayed value.
 *
 * @param image first photo of the first colorway; null when there is none
 */
public record ProductTileView(
        String slug,
        String name,
        @Nullable ProductImage image,
        EffectivePrice price,
        int colorCount,
        boolean inStock,
        boolean isNew) {}
