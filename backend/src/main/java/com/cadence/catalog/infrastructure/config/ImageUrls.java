package com.cadence.catalog.infrastructure.config;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.springframework.stereotype.Component;

/**
 * Turns an image key into the URL the browser loads (BR-CAT-01). The only place that knows where
 * images live, so moving to object storage (architecture 9.7) changes configuration, not data.
 */
@Component
public class ImageUrls {

    private final String template;

    public ImageUrls(CatalogProperties properties) {
        this.template = properties.imageUrlTemplate();
    }

    public String urlOf(String imageKey) {
        // A key is a path: keep its slashes, encode anything else that could break the URL.
        String encoded =
                URLEncoder.encode(imageKey, StandardCharsets.UTF_8)
                        .replace("%2F", "/")
                        .replace("+", "%20");
        return template.replace("{key}", encoded);
    }
}
