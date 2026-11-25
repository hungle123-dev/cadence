package com.cadence.catalog.application.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.jspecify.annotations.Nullable;

import com.cadence.catalog.domain.model.ColorFamily;
import com.cadence.catalog.domain.model.Colorway;
import com.cadence.catalog.domain.model.Fit;
import com.cadence.catalog.domain.model.Gender;
import com.cadence.catalog.domain.model.Product;
import com.cadence.catalog.domain.model.ProductImage;
import com.cadence.catalog.domain.model.Spec;
import com.cadence.catalog.domain.model.Sport;
import com.cadence.catalog.domain.model.Variant;
import com.cadence.catalog.domain.valueobject.EffectivePrice;
import com.cadence.catalog.domain.valueobject.Visibility;

/**
 * The PDP (FR-CAT-05): a product that is on sale or coming soon, worked out for one moment. The web
 * layer only turns it into JSON (image keys become URLs there, BR-CAT-01).
 *
 * @param visibility ON_SALE or COMING_SOON; the page shows a countdown and noindex for the second
 * @param launchAt the countdown target while coming soon
 */
public record ProductDetailView(
        String slug,
        String name,
        CategoryView category,
        Gender gender,
        Sport sport,
        String description,
        List<Spec> specs,
        Fit fit,
        @Nullable BigDecimal rating,
        int reviewCount,
        EffectivePrice price,
        boolean inStock,
        boolean isNew,
        Visibility visibility,
        Instant launchAt,
        String seoTitle,
        String seoDescription,
        List<ColorwayView> colorways) {

    public ProductDetailView {
        specs = List.copyOf(specs);
        colorways = List.copyOf(colorways);
    }

    /** The product as a customer sees it at {@code now}; the caller already checked R-VIS. */
    public static ProductDetailView from(Product product, CategoryView category, Instant now) {
        return new ProductDetailView(
                product.slug(),
                product.name(),
                category,
                product.gender(),
                product.sport(),
                product.description(),
                product.specs(),
                product.fit(),
                product.rating(),
                product.reviewCount(),
                product.priceAt(now),
                product.isInStock(),
                product.isNewAt(now),
                product.visibilityAt(now),
                product.publication().launchAt(),
                product.seoTitle(category.name()),
                product.seoDescription(),
                product.colorways().stream().map(ColorwayView::from).toList());
    }

    /** In display order; the first is the default colorway. */
    public record ColorwayView(
            UUID id,
            String name,
            ColorFamily colorFamily,
            boolean inStock,
            List<ProductImage> images,
            List<Variant> sizes) {

        public ColorwayView {
            images = List.copyOf(images);
            sizes = List.copyOf(sizes);
        }

        static ColorwayView from(Colorway colorway) {
            return new ColorwayView(
                    colorway.id(),
                    colorway.name(),
                    colorway.colorFamily(),
                    colorway.isInStock(),
                    colorway.images(),
                    colorway.variants());
        }
    }
}
