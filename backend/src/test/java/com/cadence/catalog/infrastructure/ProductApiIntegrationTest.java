package com.cadence.catalog.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.transaction.annotation.Transactional;

import com.cadence.TestcontainersConfiguration;

/**
 * GET /catalog/products/{slug} end to end on a real PostgreSQL 18 loaded with the local seed
 * (AC 3.1, 3.3, 3.8, 1.5). @Transactional rolls the seed back after each test.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
@Transactional
@Sql({ "classpath:db/seed/R__seed_categories.sql", "classpath:db/seed/R__seed_products.sql" })
class ProductApiIntegrationTest {

    @Autowired
    MockMvcTester mvc;

    @Test
    void returns_the_pdp_of_a_published_product() {
        var json = assertThat(mvc.get().uri("/catalog/products/tempo-2")).hasStatusOk().bodyJson();

        json.extractingPath("$.name").isEqualTo("Cadence Tempo 2");
        json.extractingPath("$.breadcrumb[*].slug").asArray().containsExactly("giay", "giay-chay-bo");
        json.extractingPath("$.colorways[*].slug").asArray().containsExactly("den", "trang", "xanh");
        json.extractingPath("$.colorways[0].price.amount").isEqualTo(1990000);
        json.extractingPath("$.colorways[0].price.compareAt").isEqualTo(2490000);
        json.extractingPath("$.colorways[1].price.compareAt").isNull();
        json.extractingPath("$.colorways[0].sizes[1].inStock").isEqualTo(false);
        json.extractingPath("$.colorways[0].images[0].url").isEqualTo("/media/products/tempo-2/den-1.jpg");
    }

    @Test
    void draft_product_answers_404() {
        var result = mvc.get().uri("/catalog/products/daily-canvas").exchange();

        assertThat(result).hasStatus(HttpStatus.NOT_FOUND);
        assertThat(result).bodyJson().extractingPath("$.code").isEqualTo("CATALOG_PRODUCT_NOT_FOUND");
    }

    @Test
    void scheduled_product_answers_404_before_launch() {
        assertThat(mvc.get().uri("/catalog/products/canvas-low")).hasStatus(HttpStatus.NOT_FOUND);
    }

    @Test
    void unknown_product_answers_404() {
        assertThat(mvc.get().uri("/catalog/products/khong-co-that")).hasStatus(HttpStatus.NOT_FOUND);
    }

    // ---------- PLP (CAT-01, CAT-02) ----------

    @Test
    void plp_lists_one_tile_per_colour_including_child_categories() {
        var json = assertThat(mvc.get().uri("/catalog/products?category=giay")).hasStatusOk().bodyJson();

        json.extractingPath("$.totalItems").isEqualTo(5); // tempo-2 x3 + court-classic x2 (AC 1.1, 1.2)
        json.extractingPath("$.pageSize").isEqualTo(24);
        json.extractingPath("$.filters.genders").asArray().containsExactly("MEN", "WOMEN");
    }

    @Test
    void plp_tile_carries_the_effective_price() {
        var json = assertThat(mvc.get().uri("/catalog/products?category=giay-chay-bo&color=BLACK"))
                .hasStatusOk().bodyJson();

        json.extractingPath("$.items[0].productSlug").isEqualTo("tempo-2");
        json.extractingPath("$.items[0].price.amount").isEqualTo(1990000);
        json.extractingPath("$.items[0].price.compareAt").isEqualTo(2490000);
    }

    @Test
    void plp_page_past_the_end_is_empty_but_still_counts() {
        var json = assertThat(mvc.get().uri("/catalog/products?category=giay&page=99")).hasStatusOk().bodyJson();

        json.extractingPath("$.items").asArray().isEmpty();
        json.extractingPath("$.totalItems").isEqualTo(5);
    }

    @Test
    void plp_unknown_category_answers_404() {
        var result = mvc.get().uri("/catalog/products?category=khong-co-that").exchange();

        assertThat(result).hasStatus(HttpStatus.NOT_FOUND);
        assertThat(result).bodyJson().extractingPath("$.code").isEqualTo("CATALOG_CATEGORY_NOT_FOUND");
    }

    @Test
    void plp_bad_values_answer_400_with_every_parameter() {
        var result = mvc.get().uri("/catalog/products?color=XYZ&sort=abc").exchange();

        assertThat(result).hasStatus(HttpStatus.BAD_REQUEST);
        assertThat(result).bodyJson().extractingPath("$.code").isEqualTo("CATALOG_INVALID_FILTER");
        assertThat(result).bodyJson().extractingPath("$.invalidParams[*].name").asArray()
                .containsExactlyInAnyOrder("color", "sort");
    }

    @Test
    void plp_out_of_range_values_answer_400() {
        for (String bad : List.of("minPrice=-5", "minPrice=500000&maxPrice=100000", "page=0", "page=abc", "size=abc")) {
            var result = mvc.get().uri("/catalog/products?" + bad).exchange();

            assertThat(result).as(bad).hasStatus(HttpStatus.BAD_REQUEST);
            assertThat(result).as(bad).bodyJson().extractingPath("$.code").isEqualTo("CATALOG_INVALID_FILTER");
        }
    }
}
