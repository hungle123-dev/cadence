package com.cadence.catalog.infrastructure.restapi.uidto.response;

import com.cadence.catalog.application.dto.CategoryView;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "Category")
public record CategoryResponse(
        @Schema(example = "giay-chay-bo") String slug,
        @Schema(example = "Giày chạy bộ") String name) {

    public static CategoryResponse from(CategoryView category) {
        return new CategoryResponse(category.slug(), category.name());
    }
}
