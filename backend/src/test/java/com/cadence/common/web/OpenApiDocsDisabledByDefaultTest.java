package com.cadence.common.web;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

import com.cadence.TestcontainersConfiguration;

/** ADR-0006 + 7.5 A10: without SPRINGDOC_ENABLED (production) the spec and Swagger UI are closed. */
@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class OpenApiDocsDisabledByDefaultTest {

    @Autowired
    MockMvcTester mvc;

    @Test
    void api_docs_are_not_served() {
        assertThat(mvc.get().uri("/v3/api-docs")).hasStatus4xxClientError();
    }

    @Test
    void swagger_ui_is_not_served() {
        assertThat(mvc.get().uri("/swagger-ui/index.html")).hasStatus4xxClientError();
    }
}
