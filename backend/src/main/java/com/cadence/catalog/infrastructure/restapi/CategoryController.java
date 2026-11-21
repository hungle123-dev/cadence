package com.cadence.catalog.infrastructure.restapi;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cadence.catalog.application.dto.CategoryView;
import com.cadence.catalog.application.port.driving.ListCategoriesUseCase;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = CategoryController.TAG, description = "Browse what is for sale: categories, PLP, PDP")
@RestController
@RequestMapping("/catalog/categories")
class CategoryController {

    static final String TAG = "catalog";

    private final ListCategoriesUseCase listCategories;

    CategoryController(ListCategoriesUseCase listCategories) {
        this.listCategories = listCategories;
    }

    @Operation(summary = "Whole category tree, flat, ordered by depth then position (menu, breadcrumb)")
    @GetMapping
    List<CategoryView> list() {
        return listCategories.listCategories();
    }
}