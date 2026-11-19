package com.cadence.catalog.infrastructure.restapi;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cadence.catalog.application.dto.CategoryView;
import com.cadence.catalog.application.port.driving.ListCategoriesUseCase;

@RestController
@RequestMapping("/catalog/categories")
class CategoryController {

    private final ListCategoriesUseCase listCategories;

    CategoryController(ListCategoriesUseCase listCategories) {
        this.listCategories = listCategories;
    }

    @GetMapping
    List<CategoryView> list() {
        return listCategories.listCategories();
    }
}