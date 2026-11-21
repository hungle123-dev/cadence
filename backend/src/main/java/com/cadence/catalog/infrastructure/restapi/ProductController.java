package com.cadence.catalog.infrastructure.restapi;

import java.util.List;

import org.jspecify.annotations.Nullable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.cadence.catalog.application.dto.ProductSort;
import com.cadence.catalog.domain.model.ColorFamily;
import com.cadence.catalog.domain.model.Gender;
import com.cadence.catalog.infrastructure.restapi.uidto.ProductDetailResponse;
import com.cadence.catalog.infrastructure.restapi.uidto.ProductTilePageResponse;
import com.cadence.catalog.infrastructure.restapi.uidto.SitemapResponse;
import com.cadence.common.web.ApiProblem;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Slice 1a read endpoints (section 7.5). L2: signatures and contract only; every method answers
 * 501 until the use cases exist (L3).
 */
@Tag(name = CategoryController.TAG)
@RestController
@RequestMapping("/catalog")
class ProductController {

    @Operation(summary = "PLP: one tile per colorway (CAT-01, CAT-02)", description = """
            Only visible products (R-VIS). A category includes its sub-categories.
            Values of one filter are OR-ed, different filters are AND-ed.
            Price filter and price sort use the effective price (R-PRICE).
            Page size is fixed at 24; a page past the last one returns 200 with empty items (A2).""")
    @ApiResponse(responseCode = "200", description = "OK")
    @ApiResponse(responseCode = "400", description = "CATALOG_INVALID_FILTER, with invalidParams (AC 2.11)",
            content = @Content(mediaType = ApiProblem.MEDIA_TYPE, schema = @Schema(implementation = ApiProblem.class)))
    @ApiResponse(responseCode = "404", description = "CATALOG_CATEGORY_NOT_FOUND (AC 1.9)",
            content = @Content(mediaType = ApiProblem.MEDIA_TYPE, schema = @Schema(implementation = ApiProblem.class)))
    @GetMapping("/products")
    ProductTilePageResponse listProducts(
            @Parameter(description = "Category slug; omitted = all products", example = "giay-chay-bo")
            @RequestParam(required = false) @Nullable String category,
            @Parameter(description = "Repeatable (size=42&size=43); keeps colorways where that size is in stock (R-STOCK)")
            @RequestParam(required = false) @Nullable List<String> size,
            @Parameter(description = "Repeatable")
            @RequestParam(required = false) @Nullable List<ColorFamily> color,
            @Parameter(description = "UNISEX products match both", schema = @Schema(allowableValues = { "MEN", "WOMEN" }))
            @RequestParam(required = false) @Nullable Gender gender,
            @Parameter(description = "Integer VND, effective price", schema = @Schema(type = "integer", format = "int64", minimum = "0"))
            @RequestParam(required = false) @Nullable Long minPrice,
            @Parameter(description = "Integer VND, effective price, >= minPrice", schema = @Schema(type = "integer", format = "int64", minimum = "0"))
            @RequestParam(required = false) @Nullable Long maxPrice,
            @RequestParam(defaultValue = "NEWEST") ProductSort sort,
            @Parameter(description = "1-based", schema = @Schema(type = "integer", format = "int32", minimum = "1", defaultValue = "1"))
            @RequestParam(defaultValue = "1") int page) {
        throw notYetImplemented();
    }

    @Operation(summary = "PDP with every colorway, image and size (CAT-03)", description = """
            A product that is not visible (R-VIS) answers 404 exactly like an unknown slug (A7).""")
    @ApiResponse(responseCode = "200", description = "OK")
    @ApiResponse(responseCode = "404", description = "CATALOG_PRODUCT_NOT_FOUND (AC 3.8)",
            content = @Content(mediaType = ApiProblem.MEDIA_TYPE, schema = @Schema(implementation = ApiProblem.class)))
    @GetMapping("/products/{slug}")
    ProductDetailResponse getProduct(@Parameter(example = "tempo-2") @PathVariable String slug) {
        throw notYetImplemented();
    }

    @Operation(summary = "Slugs for /sitemap.xml (N4, AC 4.6)", description = "Visible products only.")
    @ApiResponse(responseCode = "200", description = "OK")
    @GetMapping("/sitemap")
    SitemapResponse sitemap() {
        throw notYetImplemented();
    }

    private static ResponseStatusException notYetImplemented() {
        return new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED, "Chưa có, làm ở L3");
    }
}
