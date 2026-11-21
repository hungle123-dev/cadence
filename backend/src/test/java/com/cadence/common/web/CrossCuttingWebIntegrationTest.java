package com.cadence.common.web;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.cadence.TestcontainersConfiguration;
import com.cadence.common.domain.error.ConflictException;
import com.cadence.common.domain.error.InvalidParam;
import com.cadence.common.domain.error.NotFoundException;
import com.cadence.common.domain.error.ValidationException;

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
		assertThat(result).bodyJson().extractingPath("$.code").isEqualTo("NOT_FOUND");
	}

	@Test
	void malformed_parameter_returns_a_400_problem_with_code_bad_request() {
		MvcTestResult result = mvc.get().uri("/test/page?page=abc").exchange();

		assertThat(result).hasStatus(HttpStatus.BAD_REQUEST)
				.hasContentType(MediaType.APPLICATION_PROBLEM_JSON);
		assertThat(result).bodyJson().extractingPath("$.code").isEqualTo("BAD_REQUEST");
	}

	@Test
	void wrong_http_method_returns_a_405_problem_with_a_code() {
		MvcTestResult result = mvc.post().uri("/catalog/categories").exchange();

		assertThat(result).hasStatus(HttpStatus.METHOD_NOT_ALLOWED);
		assertThat(result).bodyJson().extractingPath("$.code").isEqualTo("METHOD_NOT_ALLOWED");
	}

	@Test
	void unexpected_error_returns_a_generic_500_without_internal_details() {
		MvcTestResult result = mvc.get().uri("/test/boom")
				.header(CorrelationIdFilter.HEADER, "trace-500").exchange();

		assertThat(result).hasStatus(HttpStatus.INTERNAL_SERVER_ERROR)
				.hasContentType(MediaType.APPLICATION_PROBLEM_JSON);
		assertThat(result).bodyJson().extractingPath("$.correlationId").isEqualTo("trace-500");
		assertThat(result).bodyJson().extractingPath("$.code").isEqualTo("INTERNAL_ERROR");
		assertThat(result).bodyText().doesNotContain("secret internal detail").doesNotContain("IllegalStateException");
	}

	// ---------- Business errors (DomainException) ----------

	@Test
	void not_found_business_error_returns_404_with_its_own_code_and_message() {
		MvcTestResult result = mvc.get().uri("/test/not-found").exchange();

		assertThat(result).hasStatus(HttpStatus.NOT_FOUND)
				.hasContentType(MediaType.APPLICATION_PROBLEM_JSON);
		assertThat(result).bodyJson().extractingPath("$.code").isEqualTo("TEST_THING_NOT_FOUND");
		assertThat(result).bodyJson().extractingPath("$.detail").isEqualTo("Không tìm thấy");
		assertThat(result).bodyJson().extractingPath("$.correlationId").isNotNull();
	}

	@Test
	void validation_business_error_returns_400_with_invalid_params() {
		MvcTestResult result = mvc.get().uri("/test/invalid").exchange();

		assertThat(result).hasStatus(HttpStatus.BAD_REQUEST);
		assertThat(result).bodyJson().extractingPath("$.code").isEqualTo("TEST_INVALID");
		assertThat(result).bodyJson().extractingPath("$.invalidParams[0].name").isEqualTo("maxPrice");
		assertThat(result).bodyJson().extractingPath("$.invalidParams[0].reason").isEqualTo("phải lớn hơn minPrice");
	}

	@Test
	void conflict_business_error_returns_409() {
		MvcTestResult result = mvc.get().uri("/test/conflict").exchange();

		assertThat(result).hasStatus(HttpStatus.CONFLICT);
		assertThat(result).bodyJson().extractingPath("$.code").isEqualTo("TEST_CONFLICT");
	}

	static class ThingNotFoundException extends NotFoundException {
		ThingNotFoundException() {
			super("TEST_THING_NOT_FOUND", "Không tìm thấy");
		}
	}

	static class InvalidThingException extends ValidationException {
		InvalidThingException() {
			super("TEST_INVALID", "Không hợp lệ", List.of(new InvalidParam("maxPrice", "phải lớn hơn minPrice")));
		}
	}

	static class ThingConflictException extends ConflictException {
		ThingConflictException() {
			super("TEST_CONFLICT", "Xung đột");
		}
	}

	/** Test-only endpoints: unexpected exception, typed parameter, and one per business error kind. */
	@RestController
	static class FailingController {

		@GetMapping("/test/not-found")
		String notFound() {
			throw new ThingNotFoundException();
		}

		@GetMapping("/test/invalid")
		String invalid() {
			throw new InvalidThingException();
		}

		@GetMapping("/test/conflict")
		String conflict() {
			throw new ThingConflictException();
		}

		@GetMapping("/test/page")
		String page(@RequestParam int page) {
			return "page " + page;
		}

		@GetMapping("/test/boom")
		String boom() {
			throw new IllegalStateException("secret internal detail");
		}
	}
}
