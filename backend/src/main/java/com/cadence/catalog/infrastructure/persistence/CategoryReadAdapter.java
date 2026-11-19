package com.cadence.catalog.infrastructure.persistence;

import java.util.List;

import com.cadence.catalog.application.dto.CategoryView;
import com.cadence.catalog.application.port.driven.CategoryReadPort;
import com.cadence.catalog.infrastructure.persistence.entity.CategoryJpaEntity;
import com.cadence.catalog.infrastructure.persistence.repository.CategoryJpaRepository;

public class CategoryReadAdapter implements CategoryReadPort {

    private final CategoryJpaRepository repository;

    public CategoryReadAdapter(CategoryJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<CategoryView> findAllOrdered() {
        return repository.findAllByOrderByDepthAscPositionAsc().stream()
                .map(CategoryReadAdapter::toView)
                .toList();
    }

    private static CategoryView toView(CategoryJpaEntity e) {
        return new CategoryView(e.getId(), e.getParentId(), e.getSlug(), e.getName(), e.getDepth(), e.getPosition());
    }
}