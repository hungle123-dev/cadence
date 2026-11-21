package com.cadence.catalog.application.port.driven;

import java.util.Optional;

import com.cadence.catalog.application.dto.ProductSnapshot;

public interface ProductDetailReadPort {

    /** Any status: deciding visibility is the use case's job (R-VIS), not the database's. */
    Optional<ProductSnapshot> findBySlug(String slug);
}
