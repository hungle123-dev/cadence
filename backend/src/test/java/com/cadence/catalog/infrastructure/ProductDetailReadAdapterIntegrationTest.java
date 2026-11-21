package com.cadence.catalog.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import org.hibernate.SessionFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import com.cadence.TestcontainersConfiguration;
import com.cadence.catalog.application.dto.ProductSnapshot;
import com.cadence.catalog.application.port.driven.ProductDetailReadPort;
import com.cadence.catalog.domain.model.ProductStatus;

import jakarta.persistence.EntityManagerFactory;

/**
 * ProductDetailReadAdapter on a real PostgreSQL 18, loaded with the local seed.
 * @Transactional rolls everything back after each test, so other tests start from empty tables.
 */
@SpringBootTest(properties = "spring.jpa.properties.hibernate.generate_statistics=true")
@Import(TestcontainersConfiguration.class)
@Transactional
@Sql({ "classpath:db/seed/R__seed_categories.sql", "classpath:db/seed/R__seed_products.sql" })
class ProductDetailReadAdapterIntegrationTest {

    @Autowired
    ProductDetailReadPort port;

    @Autowired
    EntityManagerFactory entityManagerFactory;

    @Test
    void reads_tempo_2_as_stored() {
        ProductSnapshot tempo2 = port.findBySlug("tempo-2").orElseThrow();

        assertThat(tempo2.publication().status()).isEqualTo(ProductStatus.PUBLISHED);
        assertThat(tempo2.specs()).containsEntry("Trọng lượng", "245 g");
        assertThat(tempo2.colorways()).extracting(ProductSnapshot.Colorway::slug)
                .containsExactly("den", "trang", "xanh");

        ProductSnapshot.Colorway den = tempo2.colorways().getFirst();
        assertThat(den.pricing().sale()).isNotNull();
        assertThat(den.images()).hasSize(1);
        assertThat(den.sizes()).extracting(ProductSnapshot.Size::size).containsExactly("40", "41", "42");
        assertThat(den.sizes()).extracting(ProductSnapshot.Size::inStock).containsExactly(true, false, true);
    }

    @Test
    void unknown_slug_is_empty() {
        assertThat(port.findBySlug("khong-co-that")).isEmpty();
    }

    @Test
    void reads_a_product_in_a_constant_number_of_queries() {
        var statistics = entityManagerFactory.unwrap(SessionFactory.class).getStatistics();
        statistics.clear();

        port.findBySlug("tempo-2").orElseThrow();

        // product+colorways, variants (batch), images (batch), stock flags: no N+1
        assertThat(statistics.getPrepareStatementCount()).isLessThanOrEqualTo(4);
    }
}
