package com.cadence.catalog.infrastructure.config;

import java.time.Clock;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.cadence.catalog.application.port.driven.CategoryReadPort;
import com.cadence.catalog.application.port.driven.ProductDetailReadPort;
import com.cadence.catalog.application.port.driving.ListCategoriesUseCase;
import com.cadence.catalog.application.port.driving.ProductQueryUseCase;
import com.cadence.catalog.application.service.ListCategoriesService;
import com.cadence.catalog.application.service.ProductDetailViewAssembler;
import com.cadence.catalog.application.service.ProductQueryService;
import com.cadence.catalog.infrastructure.adapter.CategoryReadAdapter;
import com.cadence.catalog.infrastructure.adapter.ProductDetailReadAdapter;
import com.cadence.catalog.infrastructure.persistence.repository.CategoryJpaRepository;
import com.cadence.catalog.infrastructure.persistence.repository.ProductJpaRepository;
import com.cadence.catalog.infrastructure.persistence.repository.VariantStockFlagJpaRepository;

/** Composition root of the catalog module: which adapter backs which port, which service backs which use case. */
@Configuration
class CatalogConfiguration {

    // ---------- Driven ports -> adapters ----------

    @Bean
    CategoryReadPort categoryReadPort(CategoryJpaRepository repository) {
        return new CategoryReadAdapter(repository);
    }

    @Bean
    ProductDetailReadPort productDetailReadPort(ProductJpaRepository products, VariantStockFlagJpaRepository stockFlags) {
        return new ProductDetailReadAdapter(products, stockFlags);
    }

    // ---------- Driving ports -> services ----------

    @Bean
    ListCategoriesUseCase listCategoriesUseCase(CategoryReadPort categoryReadPort) {
        return new ListCategoriesService(categoryReadPort);
    }

    @Bean
    ProductQueryUseCase productQueryUseCase(ProductDetailReadPort products, CategoryReadPort categories, Clock clock) {
        return new ProductQueryService(products, new ProductDetailViewAssembler(categories), clock);
    }
}
