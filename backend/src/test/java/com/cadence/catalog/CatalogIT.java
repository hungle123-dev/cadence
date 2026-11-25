package com.cadence.catalog;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import com.cadence.PostgresIT;

/**
 * Base of the catalog integration tests: the sample catalog plus a few edge-case products, read at
 * a fixed moment, inside a transaction rolled back after each test. One shared configuration, so
 * every catalog test reuses one application context and one PostgreSQL container.
 */
@AutoConfigureMockMvc
@Import(CatalogIT.CatalogTestClock.class)
@Sql({"/db/seed/dev/R__seed_catalog.sql", "/sql/catalog-fixtures.sql"})
@Transactional
public abstract class CatalogIT extends PostgresIT {

    /** The demo date: "Mới" products of the sample data launched five days earlier. */
    protected static final Instant NOW = Instant.parse("2026-11-25T03:00:00Z");

    @TestConfiguration(proxyBeanMethods = false)
    static class CatalogTestClock {

        @Bean
        @Primary
        Clock fixedClock() {
            return Clock.fixed(NOW, ZoneOffset.UTC);
        }
    }
}
