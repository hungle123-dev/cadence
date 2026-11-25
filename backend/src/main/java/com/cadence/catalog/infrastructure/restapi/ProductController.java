package com.cadence.catalog.infrastructure.restapi;

import jakarta.validation.Valid;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cadence.catalog.application.port.driving.ProductQueryUseCase;
import com.cadence.catalog.infrastructure.config.ImageUrls;
import com.cadence.catalog.infrastructure.restapi.uidto.ProductListRequest;
import com.cadence.catalog.infrastructure.restapi.uidto.response.FacetsResponse;
import com.cadence.catalog.infrastructure.restapi.uidto.response.ProductDetailResponse;
import com.cadence.catalog.infrastructure.restapi.uidto.response.ProductTileResponse;
import com.cadence.catalog.infrastructure.restapi.uidto.response.SitemapResponse;
import com.cadence.common.web.response.UIApiResponse;
import com.cadence.common.web.response.UIPageResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

/** Product reads: list, filter panel, PDP, sitemap. */
@Tag(name = CategoryController.TAG)
@RestController("catalogProductController")
@RequestMapping("/v1/catalog")
@RequiredArgsConstructor
public class ProductController {

    private final ProductQueryUseCase productQuery;
    private final ImageUrls imageUrls;

    @Operation(summary = "Products on sale, filtered, sorted, 12 per page (FR-CAT-01…03)")
    @GetMapping("/products")
    public ResponseEntity<UIPageResponse<ProductTileResponse>> list(
            @ParameterObject @Valid ProductListRequest request) {
        return ResponseEntity.ok(
                UIPageResponse.from(
                        productQuery.listProducts(request.toQuery()),
                        tile -> ProductTileResponse.from(tile, imageUrls)));
    }

    // A literal path wins over /products/{slug}, so a product can never use the slug "facets".
    @Operation(
            summary = "Filter panel with a count per choice, for the same parameters (FR-CAT-02)")
    @GetMapping("/products/facets")
    public ResponseEntity<UIApiResponse<FacetsResponse>> facets(
            @ParameterObject @Valid ProductListRequest request) {
        return ResponseEntity.ok(
                UIApiResponse.of(FacetsResponse.from(productQuery.getFacets(request.toQuery()))));
    }

    @Operation(
            summary =
                    "PDP of a product on sale or coming soon; draft and archived are a 404"
                            + " (FR-CAT-05, FR-CAT-07, NFR-SEO-06)")
    @GetMapping("/products/{slug}")
    public ResponseEntity<UIApiResponse<ProductDetailResponse>> get(@PathVariable String slug) {
        return ResponseEntity.ok(
                UIApiResponse.of(
                        ProductDetailResponse.from(
                                productQuery.getProductDetail(slug), imageUrls)));
    }

    @Operation(summary = "Slugs for /sitemap.xml: categories and products on sale (NFR-SEO-04)")
    @GetMapping("/sitemap")
    public ResponseEntity<UIApiResponse<SitemapResponse>> sitemap() {
        return ResponseEntity.ok(UIApiResponse.of(SitemapResponse.from(productQuery.getSitemap())));
    }
}
