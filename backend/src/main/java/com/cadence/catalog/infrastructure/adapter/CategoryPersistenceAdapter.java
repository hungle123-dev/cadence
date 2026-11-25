package com.cadence.catalog.infrastructure.adapter;

import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.cadence.catalog.application.dto.CategoryView;
import com.cadence.catalog.application.port.driven.CategoryReadPort;
import com.cadence.catalog.infrastructure.persistence.repository.CategoryJpaRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CategoryPersistenceAdapter implements CategoryReadPort {

    private final CategoryJpaRepository categoryRepository;

    @Override
    @Transactional(readOnly = true)
    public List<CategoryView> findAllOrdered() {
        return categoryRepository.findAllByOrderBySortOrderAscSlugAsc().stream()
                .map(category ->
                        new CategoryView(category.getId(), category.getSlug(), category.getName()))
                .toList();
    }
}
