package com.cadence.catalog.infrastructure.restapi.uidto.response;

import org.jspecify.annotations.Nullable;

import com.cadence.catalog.domain.valueobject.EffectivePrice;

import io.swagger.v3.oas.annotations.media.Schema;

/** The web app only displays this; it never works out a price itself (BR-PRC-01). */
@Schema(name = "Price", description = "Effective price, integer VND")
public record PriceResponse(
        @Schema(description = "What the customer pays now", example = "2290000") long amount,
        @Schema(
                        description = "List price, struck through; only while a sale is active",
                        example = "2890000",
                        types = {"integer", "null"})
                @Nullable Long compareAt,
        @Schema(
                        description = "The −N% label (FR-CAT-09), rounded down; 0 without a sale",
                        example = "20")
                int discountPercent) {

    public static PriceResponse from(EffectivePrice price) {
        return new PriceResponse(price.amount(), price.compareAt(), price.discountPercent());
    }
}
