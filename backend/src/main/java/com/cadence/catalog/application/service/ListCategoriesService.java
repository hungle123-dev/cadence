package com.cadence.catalog.application.service;

import java.util.List;

import com.cadence.catalog.application.dto.CategoryView;
import com.cadence.catalog.application.port.driven.CategoryReadPort;
import com.cadence.catalog.application.port.driving.ListCategoriesUseCase;

public class ListCategoriesService implements ListCategoriesUseCase {

    private final CategoryReadPort categories;

    public ListCategoriesService(CategoryReadPort categories) {
        this.categories = categories;
    }

    @Override
    public List<CategoryView> listCategories() {
        return categories.findAllOrdered();
    }
}