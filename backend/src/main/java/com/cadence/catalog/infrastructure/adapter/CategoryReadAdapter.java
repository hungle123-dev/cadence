package com.cadence.catalog.infrastructure.adapter;

import java.util.List;

import com.cadence.catalog.application.dto.CategoryView;
import com.cadence.catalog.application.port.driven.CategoryReadPort;
import com.cadence.catalog.infrastructure.persistence.entity.CategoryJpaEntity;
import com.cadence.catalog.infrastructure.persistence.repository.CategoryJpaRepository;

public class CategoryReadAdapter implements CategoryReadPort {

    private final CategoryJpaRepository categoryRepository;

    public CategoryReadAdapter(CategoryJpaRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    public List<CategoryView> findAllOrdered() {
        return categoryRepository.findAllByOrderByDepthAscPositionAsc().stream()
                .map(CategoryReadAdapter::toView)
                .toList();
    }

    private static CategoryView toView(CategoryJpaEntity entity) {
        return new CategoryView(entity.getId(), entity.getParentId(), entity.getSlug(), entity.getName(), entity.getDepth(), entity.getPosition());
    }
}