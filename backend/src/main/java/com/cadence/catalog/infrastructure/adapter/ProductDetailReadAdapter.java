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

    private final ProductJpaRepository products;
    private final VariantStockFlagJpaRepository stockFlags;

    public ProductDetailReadAdapter(ProductJpaRepository products, VariantStockFlagJpaRepository stockFlags) {
        this.products = products;
        this.stockFlags = stockFlags;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ProductSnapshot> findBySlug(String slug) {
        return products.findBySlug(slug).map(this::toSnapshot);
    }

    private ProductSnapshot toSnapshot(ProductJpaEntity p) {
        Map<UUID, Boolean> inStock = inStockByVariant(p);
        return new ProductSnapshot(
                p.getSlug(), p.getName(), p.getDescription(),
                ProductType.valueOf(p.getType()), Gender.valueOf(p.getGender()),
                p.getSpecs(), p.getCategoryId(),
                new ProductPublication(ProductStatus.valueOf(p.getStatus()), p.getPublishAt()),
                p.getColorways().stream().map(c -> toSnapshot(c, inStock)).toList());
    }

    private static ProductSnapshot.Colorway toSnapshot(ColorwayJpaEntity c, Map<UUID, Boolean> inStock) {
        Sale sale = c.getSalePrice() == null
                ? null
                : new Sale(c.getSalePrice(), c.getSaleStartsAt(), c.getSaleEndsAt());
        return new ProductSnapshot.Colorway(
                c.getSlug(), c.getName(), ColorFamily.valueOf(c.getColorFamily()),
                new ColorwayPricing(c.getListPrice(), sale),
                c.getImages().stream()
                        .map(i -> new ProductSnapshot.Image(i.getStorageKey(), i.getAlt(), i.getWidth(), i.getHeight()))
                        .toList(),
                c.getVariants().stream()
                        .map(v -> new ProductSnapshot.Size(v.getSku(), v.getSize(), inStock.getOrDefault(v.getId(), false)))
                        .toList());
    }

    /** One query for every size of the product: variant id -> in stock. */
    private Map<UUID, Boolean> inStockByVariant(ProductJpaEntity p) {
        List<UUID> variantIds = p.getColorways().stream()
                .flatMap(c -> c.getVariants().stream())
                .map(VariantJpaEntity::getId)
                .toList();
        return stockFlags.findAllByVariantIdIn(variantIds).stream()
                .collect(Collectors.toMap(VariantStockFlagJpaEntity::getVariantId, VariantStockFlagJpaEntity::isInStock));
    }
}
