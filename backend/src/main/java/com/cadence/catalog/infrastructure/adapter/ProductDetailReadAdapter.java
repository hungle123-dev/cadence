package com.cadence.catalog.infrastructure.adapter;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.transaction.annotation.Transactional;

import com.cadence.catalog.application.dto.ProductSnapshot;
import com.cadence.catalog.application.port.driven.ProductDetailReadPort;
import com.cadence.catalog.domain.model.ColorFamily;
import com.cadence.catalog.domain.model.ColorwayPricing;
import com.cadence.catalog.domain.model.Gender;
import com.cadence.catalog.domain.model.ProductPublication;
import com.cadence.catalog.domain.model.ProductStatus;
import com.cadence.catalog.domain.model.ProductType;
import com.cadence.catalog.domain.model.Sale;
import com.cadence.catalog.infrastructure.persistence.entity.ColorwayJpaEntity;
import com.cadence.catalog.infrastructure.persistence.entity.ProductJpaEntity;
import com.cadence.catalog.infrastructure.persistence.entity.VariantJpaEntity;
import com.cadence.catalog.infrastructure.persistence.entity.VariantStockFlagJpaEntity;
import com.cadence.catalog.infrastructure.persistence.repository.ProductJpaRepository;
import com.cadence.catalog.infrastructure.persistence.repository.VariantStockFlagJpaRepository;

/** Reads one product with its colorways, sizes, images and stock flags, and translates it into a ProductSnapshot. */
public class ProductDetailReadAdapter implements ProductDetailReadPort {

    private final ProductJpaRepository productRepository;
    private final VariantStockFlagJpaRepository stockFlagRepository;

    public ProductDetailReadAdapter(ProductJpaRepository productRepository, VariantStockFlagJpaRepository stockFlagRepository) {
        this.productRepository = productRepository;
        this.stockFlagRepository = stockFlagRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ProductSnapshot> findBySlug(String slug) {
        return productRepository.findBySlug(slug).map(this::toSnapshot);
    }

    private ProductSnapshot toSnapshot(ProductJpaEntity product) {
        Map<UUID, Boolean> inStock = inStockByVariant(product);
        return new ProductSnapshot(
                product.getSlug(), product.getName(), product.getDescription(),
                ProductType.valueOf(product.getType()), Gender.valueOf(product.getGender()),
                product.getSpecs(), product.getCategoryId(),
                new ProductPublication(ProductStatus.valueOf(product.getStatus()), product.getPublishAt()),
                product.getColorways().stream().map(colorway -> toSnapshot(colorway, inStock)).toList());
    }

    private static ProductSnapshot.Colorway toSnapshot(ColorwayJpaEntity colorway, Map<UUID, Boolean> inStock) {
        Sale sale = colorway.getSalePrice() == null
                ? null
                : new Sale(colorway.getSalePrice(), colorway.getSaleStartsAt(), colorway.getSaleEndsAt());
        return new ProductSnapshot.Colorway(
                colorway.getSlug(), colorway.getName(), ColorFamily.valueOf(colorway.getColorFamily()),
                new ColorwayPricing(colorway.getListPrice(), sale),
                colorway.getImages().stream()
                        .map(image -> new ProductSnapshot.Image(image.getStorageKey(), image.getAlt(), image.getWidth(), image.getHeight()))
                        .toList(),
                colorway.getVariants().stream()
                        .map(variant -> new ProductSnapshot.Size(variant.getSku(), variant.getSize(), inStock.getOrDefault(variant.getId(), false)))
                        .toList());
    }

    /** One query for every size of the product: variant id -> in stock. */
    private Map<UUID, Boolean> inStockByVariant(ProductJpaEntity product) {
        List<UUID> variantIds = product.getColorways().stream()
                .flatMap(colorway -> colorway.getVariants().stream())
                .map(VariantJpaEntity::getId)
                .toList();
        return stockFlagRepository.findAllByVariantIdIn(variantIds).stream()
                .collect(Collectors.toMap(VariantStockFlagJpaEntity::getVariantId, VariantStockFlagJpaEntity::isInStock));
    }
}
