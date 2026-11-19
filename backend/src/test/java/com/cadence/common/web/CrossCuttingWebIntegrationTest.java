package com.cadence.common.web;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cadence.TestcontainersConfiguration;

/**
 * S6 cross-cutting web concerns: health probes (readiness includes the database), correlation id
 * on every request (ADR-0010), and RFC 9457 Problem Details for every error (ADR-0006).
 */
@SpringBootTest(properties = "management.endpoint.health.group.readiness.show-components=always")
@AutoConfigureMockMvc
@Import({ TestcontainersConfiguration.class, CrossCuttingWebIntegrationTest.FailingController.class })
class CrossCuttingWebIntegrationTest {

	private static final String UUID_PATTERN = "[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}";

	@Autowired
	MockMvcTester mvc;

	// ---------- Health ----------

	@Test
	void liveness_is_up() {
		assertThat(mvc.get().uri("/actuator/health/liveness")).hasStatusOk()
				.bodyJson().extractingPath("$.status").isEqualTo("UP");
	}

	@Test
	void readiness_is_up_and_includes_the_database() {
		var body = assertThat(mvc.get().uri("/actuator/health/readiness")).hasStatusOk().bodyJson();
		body.extractingPath("$.status").isEqualTo("UP");
		body.extractingPath("$.components.db.status").isEqualTo("UP");
	}

	// ---------- Correlation id ----------

	@Test
	void echoes_a_safe_correlation_id_sent_by_the_client() {
		assertThat(mvc.get().uri("/catalog/categories").header(CorrelationIdFilter.HEADER, "order-abc_123.x"))
				.hasStatusOk()
				.headers().hasValue(CorrelationIdFilter.HEADER, "order-abc_123.x");
	}

	@Test
	void generates_a_correlation_id_when_the_client_sends_none() {
		MvcTestResult result = mvc.get().uri("/catalog/categories").exchange();
		assertThat(result.getResponse().getHeader(CorrelationIdFilter.HEADER)).matches(UUID_PATTERN);
	}

	@Test
	void replaces_an_unsafe_correlation_id_instead_of_logging_it() {
		MvcTestResult result = mvc.get().uri("/catalog/categories")
				.header(CorrelationIdFilter.HEADER, "fake log line; DROP TABLE x").exchange();
		assertThat(result.getResponse().getHeader(CorrelationIdFilter.HEADER)).matches(UUID_PATTERN);
	}

	// ---------- Problem Details ----------

	@Test
	void unknown_path_returns_a_404_problem_with_the_correlation_id() {
		MvcTestResult result = mvc.get().uri("/catalog/khong-ton-tai")
				.header(CorrelationIdFilter.HEADER, "trace-404").exchange();

		assertThat(result).hasStatus(HttpStatus.NOT_FOUND)
				.hasContentType(MediaType.APPLICATION_PROBLEM_JSON);
		assertThat(result).bodyJson().extractingPath("$.status").isEqualTo(404);
		assertThat(result).bodyJson().extractingPath("$.correlationId").isEqualTo("trace-404");
	}

	@Test
	void unexpected_error_returns_a_generic_500_without_internal_details() {
		MvcTestResult result = mvc.get().uri("/test/boom")
				.header(CorrelationIdFilter.HEADER, "trace-500").exchange();

		assertThat(result).hasStatus(HttpStatus.INTERNAL_SERVER_ERROR)
				.hasContentType(MediaType.APPLICATION_PROBLEM_JSON);
		assertThat(result).bodyJson().extractingPath("$.correlationId").isEqualTo("trace-500");
		assertThat(result).bodyText().doesNotContain("secret internal detail").doesNotContain("IllegalStateException");
	}

	/** Test-only endpoint that fails with an unexpected exception. */
	@RestController
	static class FailingController {

		@GetMapping("/test/boom")
		String boom() {
			throw new IllegalStateException("secret internal detail");
		}
	}
}
