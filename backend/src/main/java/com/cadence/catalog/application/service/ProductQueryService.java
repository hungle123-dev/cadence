package com.cadence.catalog.application.service;

import java.time.Clock;
import java.time.Instant;

import com.cadence.catalog.application.dto.ProductDetailView;
import com.cadence.catalog.application.port.driven.ProductDetailReadPort;
import com.cadence.catalog.application.port.driving.ProductQueryUseCase;
import com.cadence.catalog.domain.exception.ProductNotFoundException;

/** Reads of products. Applies R-VIS: a hidden product answers exactly like a missing one (A7). */
public class ProductQueryService implements ProductQueryUseCase {

    private final ProductDetailReadPort products;
    private final ProductDetailViewAssembler assembler;
    private final Clock clock;

    public ProductQueryService(ProductDetailReadPort products, ProductDetailViewAssembler assembler, Clock clock) {
        this.products = products;
        this.assembler = assembler;
        this.clock = clock;
    }

    @Override
    public ProductDetailView getProductDetail(String slug) {
        Instant now = clock.instant(); // one moment for both R-VIS and R-PRICE
        return products.findBySlug(slug)
                .filter(product -> product.publication().isVisibleAt(now))
                .map(product -> assembler.toView(product, now))
                .orElseThrow(ProductNotFoundException::new);
    }
}
