package com.cadence.catalog.application.port.driven;

import java.util.Optional;

import com.cadence.catalog.domain.model.Product;

/** Loads the Product aggregate. */
public interface ProductRepositoryPort {

    /** Any status: deciding visibility is the aggregate's job (R-VIS), not the database's. */
    Optional<Product> findBySlug(String slug);
}
