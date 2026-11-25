package com.cadence.catalog.application.port.driving;

import java.util.List;

import com.cadence.catalog.application.dto.CategoryView;

public interface CategoryQueryUseCase {

    List<CategoryView> listCategories();
}
