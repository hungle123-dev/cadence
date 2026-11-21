package com.cadence.common.web;

import java.util.List;

import org.jspecify.annotations.Nullable;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Documentation-only shape of the error body, so Swagger shows the real fields. At runtime the body
 * is Spring's {@code ProblemDetail}, filled by {@link GlobalExceptionHandler}.
 */
@Schema(name = "Problem", description = "RFC 9457 Problem Details (ADR-0006)")
public record ApiProblem(
        @Schema(example = "about:blank") @Nullable String type,
        @Schema(example = "Not Found") String title,
        @Schema(example = "404") int status,
        @Schema(description = "Vietnamese, safe to show to the customer", example = "Không tìm thấy sản phẩm")
        @Nullable String detail,
        @Nullable String instance,
        @Schema(description = "Stable machine code: branch on this, never on detail",
                example = "CATALOG_PRODUCT_NOT_FOUND") String code,
        @Schema(example = "0b9c1e6a-2f4d-4c1e-9a7b-3d5e8f1a2b3c") String correlationId,
        @Schema(description = "Only for invalid input, absent otherwise") @Nullable List<Param> invalidParams) {

    public static final String MEDIA_TYPE = "application/problem+json";

    @Schema(name = "InvalidParam")
    public record Param(
            @Schema(example = "maxPrice") String name,
            @Schema(example = "phải lớn hơn hoặc bằng minPrice") String reason) {
    }
}
