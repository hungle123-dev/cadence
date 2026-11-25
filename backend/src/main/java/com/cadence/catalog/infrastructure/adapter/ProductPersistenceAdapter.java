package com.cadence.catalog.infrastructure.adapter;

import java.util.Optional;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.cadence.catalog.application.port.driven.ProductRepositoryPort;
import com.cadence.catalog.domain.model.Product;
import com.cadence.catalog.infrastructure.mapper.ProductMapper;
import com.cadence.catalog.infrastructure.persistence.repository.ProductJpaRepository;

import lombok.RequiredArgsConstructor;

/**
 * Loads a Product with its colorways, images and sizes (JPA) in three queries: product with
 * colorways, then images and variants in batches (hibernate.default_batch_fetch_size).
 */
@Component
@RequiredArgsConstructor
public class ProductPersistenceAdapter implements ProductRepositoryPort {

    private final ProductJpaRepository productRepository;
    private final ProductMapper productMapper;

    @Override
    @Transactional(readOnly = true)
    public Optional<Product> findBySlug(String slug) {
        return productRepository.findBySlug(slug).map(productMapper::toDomain);
    }
}
