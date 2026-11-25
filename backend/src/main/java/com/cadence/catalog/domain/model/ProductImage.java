package com.cadence.catalog.domain.model;

import com.cadence.common.domain.util.Preconditions;

/**
 * One photo of a colorway (BR-CAT-01). The image key, not a URL: the web layer builds the URL from
 * the configured image source, so moving to another source changes no stored data (SRS A-04). Width
 * and height let the page reserve the space before the photo loads.
 */
public record ProductImage(String imageKey, int width, int height, String alt) {

    public ProductImage {
        Preconditions.requireNotBlank(imageKey, "imageKey");
        Preconditions.requireNotBlank(alt, "alt");
        if (imageKey.contains("://")) {
            throw new IllegalArgumentException("imageKey must not contain a host: " + imageKey);
        }
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("width and height must be positive");
        }
    }
}
