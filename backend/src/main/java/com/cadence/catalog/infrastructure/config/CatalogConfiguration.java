package com.cadence.catalog.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.cadence.catalog.application.port.driven.CategoryReadPort;
import com.cadence.catalog.application.port.driving.ListCategoriesUseCase;
import com.cadence.catalog.application.service.ListCategoriesService;
import com.cadence.catalog.infrastructure.persistence.CategoryReadAdapter;
import com.cadence.catalog.infrastructure.persistence.repository.CategoryJpaRepository;

@Configuration
class CatalogConfiguration {

    @Bean
    CategoryReadPort categoryReadPort(CategoryJpaRepository repository) {
        return new CategoryReadAdapter(repository);
    }

    @Bean
    ListCategoriesUseCase listCategoriesUseCase(CategoryReadPort categoryReadPort) {
        return new ListCategoriesService(categoryReadPort);
    }
}