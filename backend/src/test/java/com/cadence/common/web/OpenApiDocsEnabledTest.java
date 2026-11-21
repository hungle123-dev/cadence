package com.cadence.common.web;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

import com.cadence.TestcontainersConfiguration;

/** ADR-0006: with SPRINGDOC_ENABLED=true the generated spec carries OpenApiConfig's info and server. */
@SpringBootTest(properties = "SPRINGDOC_ENABLED=true")
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class OpenApiDocsEnabledTest {

	@Autowired
	MockMvcTester mvc;

	@Test
	void spec_has_title_relative_server_and_catalog_endpoint() {
		var spec = assertThat(mvc.get().uri("/v3/api-docs")).hasStatusOk().bodyJson();
		spec.extractingPath("$.info.title").isEqualTo("Cadence API");
		spec.extractingPath("$.servers[0].url").isEqualTo("/api");
		spec.extractingPath("$.paths['/catalog/categories'].get").isNotNull();
	}

	@Test
	void spec_documents_the_slice_1a_catalog_contract() {
		var spec = assertThat(mvc.get().uri("/v3/api-docs")).hasStatusOk().bodyJson();
		spec.extractingPath("$.paths['/catalog/products'].get.responses['400']").isNotNull();
		spec.extractingPath("$.paths['/catalog/products/{slug}'].get.responses['404']").isNotNull();
		spec.extractingPath("$.paths['/catalog/sitemap'].get").isNotNull();
		spec.extractingPath("$.components.schemas.ProductTile.properties.colorwaySlug").isNotNull();
		spec.extractingPath("$.components.schemas.Price.properties.compareAt").isNotNull();
		spec.extractingPath("$.components.schemas.Problem.properties.code").isNotNull();
	}

	@Test
	void every_field_is_required_and_nullable_ones_say_so() {
		var spec = assertThat(mvc.get().uri("/v3/api-docs")).hasStatusOk().bodyJson();
		spec.extractingPath("$.components.schemas.Price.required").asArray().contains("amount", "compareAt");
		spec.extractingPath("$.components.schemas.Price.properties.compareAt.type").asArray().contains("null");
		spec.extractingPath("$.components.schemas.Problem.required").asArray()
				.contains("code", "correlationId").doesNotContain("invalidParams", "instance");
	}
}
