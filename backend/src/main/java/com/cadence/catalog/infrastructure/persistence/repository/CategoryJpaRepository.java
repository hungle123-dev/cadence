package com.cadence.catalog.infrastructure.persistence.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cadence.catalog.infrastructure.persistence.entity.CategoryJpaEntity;

public interface CategoryJpaRepository extends JpaRepository<CategoryJpaEntity, UUID> {

    /** Display order; slug breaks ties so the order never changes between calls. */
    List<CategoryJpaEntity> findAllByOrderBySortOrderAscSlugAsc();
}
