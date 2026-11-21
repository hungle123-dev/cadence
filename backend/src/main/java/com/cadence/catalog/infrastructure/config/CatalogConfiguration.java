package com.cadence.catalog.infrastructure.config;

import java.time.Clock;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.simple.JdbcClient;

import com.cadence.catalog.application.port.driven.CategoryReadPort;
import com.cadence.catalog.application.port.driven.ProductDetailReadPort;
import com.cadence.catalog.application.port.driven.ProductTileReadPort;
import com.cadence.catalog.application.port.driven.SitemapReadPort;
import com.cadence.catalog.application.port.driving.CategoryQueryUseCase;
import com.cadence.catalog.application.port.driving.ProductQueryUseCase;
import com.cadence.catalog.application.service.CategoryQueryService;
import com.cadence.catalog.application.service.ProductDetailViewAssembler;
import com.cadence.catalog.application.service.ProductQueryService;
import com.cadence.catalog.application.service.ProductTileViewAssembler;
import com.cadence.catalog.infrastructure.adapter.CategoryReadAdapter;
import com.cadence.catalog.infrastructure.adapter.ProductDetailReadAdapter;
import com.cadence.catalog.infrastructure.adapter.ProductTileReadAdapter;
import com.cadence.catalog.infrastructure.adapter.SitemapReadAdapter;
import com.cadence.catalog.infrastructure.persistence.repository.CategoryJpaRepository;
import com.cadence.catalog.infrastructure.persistence.repository.ProductJpaRepository;
import com.cadence.catalog.infrastructure.persistence.repository.VariantStockFlagJpaRepository;

/** Composition root of the catalog module: which adapter backs which port, which service backs which use case. */
@Configuration
class CatalogConfiguration {

    // ---------- Driven ports -> adapters ----------

    @Bean
    CategoryReadPort categoryReadPort(CategoryJpaRepository categoryRepository) {
        return new CategoryReadAdapter(categoryRepository);
    }

    @Bean
    ProductDetailReadPort productDetailReadPort(ProductJpaRepository productRepository,
            VariantStockFlagJpaRepository stockFlagRepository) {
        return new ProductDetailReadAdapter(productRepository, stockFlagRepository);
    }

    @Bean
    ProductTileReadPort productTileReadPort(JdbcClient jdbc) {
        return new ProductTileReadAdapter(jdbc);
    }

    @Bean
    SitemapReadPort sitemapReadPort(JdbcClient jdbc) {
        return new SitemapReadAdapter(jdbc);
    }

    // ---------- Driving ports -> services ----------

    @Bean
    CategoryQueryUseCase categoryQueryUseCase(CategoryReadPort categories) {
        return new CategoryQueryService(categories);
    }

    @Bean
    ProductQueryUseCase productQueryUseCase(ProductDetailReadPort productDetails, ProductTileReadPort productTiles,
            CategoryReadPort categories, SitemapReadPort sitemapEntries, Clock clock) {
        return new ProductQueryService(productDetails, new ProductDetailViewAssembler(categories), productTiles,
                categories, new ProductTileViewAssembler(), sitemapEntries, clock);
    }
}
