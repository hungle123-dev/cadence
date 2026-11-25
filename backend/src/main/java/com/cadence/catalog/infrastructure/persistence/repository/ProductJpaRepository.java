package com.cadence.catalog.infrastructure.persistence.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.cadence.catalog.infrastructure.persistence.entity.ProductJpaEntity;

public interface ProductJpaRepository extends JpaRepository<ProductJpaEntity, UUID> {

    /** Product and its colorways in one query; images and variants follow in batches. */
    @EntityGraph(attributePaths = "colorways")
    Optional<ProductJpaEntity> findBySlug(String slug);
}
