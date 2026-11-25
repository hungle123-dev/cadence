package com.cadence.catalog.infrastructure.restapi;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cadence.catalog.application.port.driving.CategoryQueryUseCase;
import com.cadence.catalog.infrastructure.restapi.uidto.response.CategoryResponse;
import com.cadence.common.web.response.UIApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = CategoryController.TAG)
@RestController("catalogCategoryController")
@RequestMapping("/v1/catalog")
@RequiredArgsConstructor
public class CategoryController {

    static final String TAG = "Catalog";

    private final CategoryQueryUseCase categoryQuery;

    @Operation(summary = "Every category, in display order")
    @GetMapping("/categories")
    public ResponseEntity<UIApiResponse<List<CategoryResponse>>> list() {
        return ResponseEntity.ok(
                UIApiResponse.of(
                        categoryQuery.listCategories().stream()
                                .map(CategoryResponse::from)
                                .toList()));
    }
}
