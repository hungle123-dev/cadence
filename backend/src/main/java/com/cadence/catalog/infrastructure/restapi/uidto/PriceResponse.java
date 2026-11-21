package com.cadence.catalog.infrastructure.restapi.uidto;

import org.jspecify.annotations.Nullable;

import com.cadence.catalog.domain.model.EffectivePrice;

import io.swagger.v3.oas.annotations.media.Schema;

/** 7.5 A5: the web app only displays this, it never computes R-PRICE itself. */
@Schema(name = "Price", description = "Effective price (R-PRICE), integer VND")
public record PriceResponse(
        @Schema(description = "What the customer pays now", example = "990000") long amount,
        @Schema(description = "List price, shown struck through; present only while a sale is active",
                example = "1290000", types = { "integer", "null" }) @Nullable Long compareAt) {

    public static PriceResponse from(EffectivePrice price) {
        return new PriceResponse(price.amount(), price.compareAt());
    }
}
