package com.cadence.catalog.application.service;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import com.cadence.catalog.application.dto.CategoryView;
import com.cadence.catalog.application.dto.PriceBand;
import com.cadence.catalog.application.dto.PriceBands;
import com.cadence.catalog.application.dto.ProductDetailView;
import com.cadence.catalog.application.dto.ProductFacetsView;
import com.cadence.catalog.application.dto.ProductListQuery;
import com.cadence.catalog.application.dto.ProductTileView;
import com.cadence.catalog.application.dto.SitemapView;
import com.cadence.catalog.application.port.driven.CategoryReadPort;
import com.cadence.catalog.application.port.driven.ProductListReadPort;
import com.cadence.catalog.application.port.driven.ProductRepositoryPort;
import com.cadence.catalog.application.port.driven.SitemapReadPort;
import com.cadence.catalog.application.port.driving.ProductQueryUseCase;
import com.cadence.catalog.domain.exception.CategoryNotFoundException;
import com.cadence.catalog.domain.exception.InvalidProductFilterException;
import com.cadence.catalog.domain.exception.ProductNotFoundException;
import com.cadence.catalog.domain.valueobject.Visibility;
import com.cadence.common.application.PageResult;
import com.cadence.common.domain.error.InvalidParam;

import lombok.RequiredArgsConstructor;

/**
 * Reads of products. Each call takes "now" once from the Clock, so visibility, price and labels
 * agree within one answer (BR-PRC-09).
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductQueryService implements ProductQueryUseCase {

    private final ProductRepositoryPort products;
    private final ProductListReadPort productList;
    private final CategoryReadPort categories;
    private final SitemapReadPort sitemapEntries;
    private final PriceBands priceBands;
    private final Clock clock;

    @Override
    public ProductDetailView getProductDetail(String slug) {
        Instant now = clock.instant();
        return products.findBySlug(slug)
                .filter(product -> product.visibilityAt(now) != Visibility.HIDDEN)
                .map(product ->
                        ProductDetailView.from(product, categoryOf(product.categoryId()), now))
                .orElseThrow(ProductNotFoundException::new);
    }

    /**
     * REPEATABLE_READ: the page and its total are two queries; one snapshot keeps "page 2 of 3" in
     * agreement with the products shown even if a product launches in between.
     */
    @Override
    @Transactional(readOnly = true, isolation = Isolation.REPEATABLE_READ)
    public PageResult<ProductTileView> listProducts(ProductListQuery query) {
        checkAgainstStoredValues(query);
        return productList.findTiles(query, clock.instant());
    }

    /** REPEATABLE_READ for the same reason: every facet group is its own query. */
    @Override
    @Transactional(readOnly = true, isolation = Isolation.REPEATABLE_READ)
    public ProductFacetsView getFacets(ProductListQuery query) {
        checkAgainstStoredValues(query);
        return productList.findFacets(query, priceBands, clock.instant());
    }

    @Override
    public PriceBands getPriceBands() {
        return priceBands;
    }

    @Override
    public SitemapView getSitemap() {
        return sitemapEntries.findSitemapEntries(clock.instant());
    }

    /**
     * Checks that need data or configuration, which the query record cannot see: the category page
     * exists (404 otherwise), category filters exist and price bands are configured ones.
     */
    private void checkAgainstStoredValues(ProductListQuery query) {
        Set<String> slugs =
                categories.findAllOrdered().stream()
                        .map(CategoryView::slug)
                        .collect(Collectors.toUnmodifiableSet());
        if (query.category() != null && !slugs.contains(query.category())) {
            throw new CategoryNotFoundException();
        }
        var errors = new ArrayList<InvalidParam>();
        for (String category : query.categories()) {
            if (!slugs.contains(category)) {
                errors.add(new InvalidParam("categories", "Danh mục không tồn tại: " + category));
            }
        }
        for (PriceBand band : query.priceBands()) {
            if (!priceBands.contains(band)) {
                errors.add(new InvalidParam("price", "Khoảng giá không hợp lệ: " + band));
            }
        }
        if (!errors.isEmpty()) {
            throw new InvalidProductFilterException(List.copyOf(errors));
        }
    }

    private CategoryView categoryOf(UUID categoryId) {
        return categories.findAllOrdered().stream()
                .filter(category -> category.id().equals(categoryId))
                .findFirst()
                .orElseThrow(() ->
                        new IllegalStateException(
                                "product refers to a missing category " + categoryId));
    }
}
