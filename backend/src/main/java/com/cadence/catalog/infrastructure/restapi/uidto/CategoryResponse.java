package com.cadence.catalog.infrastructure.restapi.uidto;

import java.util.UUID;

import org.jspecify.annotations.Nullable;

import com.cadence.catalog.application.dto.CategoryView;

import io.swagger.v3.oas.annotations.media.Schema;

/** One category of the menu tree. Exists so the spec can say parentId is null for a root (L2 leftover). */
@Schema(name = "Category")
public record CategoryResponse(
        UUID id,
        @Schema(description = "Null for a root category", types = { "string", "null" }, format = "uuid") @Nullable UUID parentId,
        @Schema(example = "giay-chay-bo") String slug,
        @Schema(example = "Giày chạy bộ") String name,
        @Schema(description = "1 for a root, at most 3", example = "2") int depth,
        @Schema(description = "Order among siblings", example = "1") int position) {

    public static CategoryResponse from(CategoryView view) {
        return new CategoryResponse(view.id(), view.parentId(), view.slug(), view.name(), view.depth(),
                view.position());
    }
}
