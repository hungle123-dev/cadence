package com.cadence.catalog.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

import com.cadence.TestcontainersConfiguration;

/**
 * Integration test for GET /catalog/categories: the whole application runs against a real
 * PostgreSQL 18 started by Testcontainers (migrations included), only HTTP is simulated by MockMvc.
 * MockMvc calls the controller directly, so the servlet context path (/api) is not part of the URI.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class CategoryApiIntegrationTest {

    private static final String SHOES = "00000000-0000-7000-8000-000000000001";
    private static final String APPAREL = "00000000-0000-7000-8000-000000000002";
    private static final String RUNNING_SHOES = "00000000-0000-7000-8000-000000000011";

    @Autowired
    MockMvcTester mvc;

    @Autowired
    JdbcTemplate jdbc;

    @BeforeEach
    void cleanCategories() {
        jdbc.update("DELETE FROM catalog.categories");
    }

    @Test
    void lists_categories_ordered_by_depth_then_position() {
        // Inserted out of order on purpose: the API must sort them, not return insertion order
        insert(APPAREL, null, "quan-ao", "Quần áo", 1, 2);
        insert(SHOES, null, "giay", "Giày", 1, 1);
        insert(RUNNING_SHOES, SHOES, "giay-chay-bo", "Giày chạy bộ", 2, 1);

        var response = assertThat(mvc.get().uri("/catalog/categories")).hasStatusOk().bodyJson();

        response.extractingPath("$[*].slug").asArray().containsExactly("giay", "quan-ao", "giay-chay-bo");
        response.extractingPath("$[2].parentId").isEqualTo(SHOES);
        response.extractingPath("$[0].parentId").isNull();
    }

    @Test
    void returns_an_empty_array_when_there_are_no_categories() {
        assertThat(mvc.get().uri("/catalog/categories")).hasStatusOk().bodyJson().isEqualTo("[]");
    }

    private void insert(String id, String parentId, String slug, String name, int depth, int position) {
        jdbc.update("""
                INSERT INTO catalog.categories (id, parent_id, slug, name, depth, position, created_at, updated_at)
                VALUES (?::uuid, ?::uuid, ?, ?, ?, ?, now(), now())
                """, id, parentId, slug, name, depth, position);
    }
}
