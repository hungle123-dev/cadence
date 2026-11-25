package com.cadence.catalog.infrastructure.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.cadence.catalog.application.dto.PriceBands;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(CatalogProperties.class)
class CatalogConfiguration {

    @Bean
    PriceBands priceBands(CatalogProperties properties) {
        return PriceBands.fromBoundaries(properties.priceBandBoundaries());
    }
}
