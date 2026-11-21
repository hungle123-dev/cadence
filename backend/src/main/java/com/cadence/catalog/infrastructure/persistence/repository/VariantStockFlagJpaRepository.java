package com.cadence.catalog.infrastructure.persistence.repository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cadence.catalog.infrastructure.persistence.entity.VariantStockFlagJpaEntity;

public interface VariantStockFlagJpaRepository extends JpaRepository<VariantStockFlagJpaEntity, UUID> {

    List<VariantStockFlagJpaEntity> findAllByVariantIdIn(Collection<UUID> variantIds);
}
