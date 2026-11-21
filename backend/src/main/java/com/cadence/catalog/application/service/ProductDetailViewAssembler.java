package com.cadence.catalog.application.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.cadence.catalog.application.dto.CategoryView;
import com.cadence.catalog.application.dto.ProductDetailView;
import com.cadence.catalog.application.dto.ProductSnapshot;
import com.cadence.catalog.application.port.driven.CategoryReadPort;

/** Turns a stored product into the PDP view for a given moment (template 4.2: Assembler). */
public class ProductDetailViewAssembler {

    private final CategoryReadPort categories;

    public ProductDetailViewAssembler(CategoryReadPort categories) {
        this.categories = categories;
    }

    public ProductDetailView toView(ProductSnapshot product, Instant now) {
        return new ProductDetailView(
                product.slug(), product.name(), product.description(), product.type(), product.gender(),
                product.specs(),
                breadcrumbFor(product.categoryId()),
                product.colorways().stream().map(colorway -> toView(colorway, now)).toList());
    }

    private ProductDetailView.Colorway toView(ProductSnapshot.Colorway colorway, Instant now) {
        return new ProductDetailView.Colorway(
                colorway.slug(), colorway.name(), colorway.colorFamily(),
                colorway.pricing().effectivePriceAt(now),                           // R-PRICE
                colorway.sizes().stream().anyMatch(ProductSnapshot.Size::inStock), // R-STOCK
                colorway.images(),
                colorway.sizes());
    }

    /** Walks up from the product's category to the root; addFirst keeps the root first. */
    private List<ProductDetailView.Breadcrumb> breadcrumbFor(UUID categoryId) {
        Map<UUID, CategoryView> byId = categories.findAllOrdered().stream()
                .collect(Collectors.toMap(CategoryView::id, Function.identity()));
        List<ProductDetailView.Breadcrumb> path = new ArrayList<>();
        for (CategoryView c = byId.get(categoryId); c != null; c = byId.get(c.parentId())) {
            path.addFirst(new ProductDetailView.Breadcrumb(c.slug(), c.name()));
        }
        return path;
    }
}
