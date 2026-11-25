package com.cadence.common.web;

import static org.assertj.core.api.Assertions.assertThat;

import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cadence.PostgresIT;
import com.cadence.common.web.response.UIApiResponse;

import io.swagger.v3.oas.annotations.media.Schema;

/** SRS NFR-MNT-04: the spec is generated from the code, and closed unless explicitly enabled. */
class OpenApiDocsTest {

    @Nested
    @AutoConfigureMockMvc
    class DisabledByDefault extends PostgresIT {

        @Autowired private MockMvcTester mvc;

        @Test
        void apiDocsAndSwaggerUi_areNotServed() {
            assertThat(mvc.get().uri("/v3/api-docs")).hasStatus4xxClientError();
            assertThat(mvc.get().uri("/swagger-ui/index.html")).hasStatus4xxClientError();
        }
    }

    @Nested
    @SpringBootTest(properties = "API_DOCS_ENABLED=true")
    @AutoConfigureMockMvc
    @Import(ProbeApi.class)
    class Enabled extends PostgresIT {

        @Autowired private MockMvcTester mvc;

        @Test
        void spec_hasTitleAndRelativeServer() {
            var spec = assertThat(mvc.get().uri("/v3/api-docs")).hasStatusOk().bodyJson();
            spec.extractingPath("$.info.title").isEqualTo("Cadence API");
            spec.extractingPath("$.servers[0].url").isEqualTo("/api");
        }

        @Test
        void spec_documentsProblemResponsesOnEveryOperation() {
            var spec = assertThat(mvc.get().uri("/v3/api-docs")).hasStatusOk().bodyJson();
            var responses = "$.paths['/test/openapi-probe'].get.responses";
            spec.extractingPath(responses + "['200'].content['application/json']").isNotNull();
            spec.extractingPath(responses + "['404'].content['application/problem+json']")
                    .isNotNull();
            spec.extractingPath("$.components.schemas.Problem.properties.requestId").isNotNull();
        }

        @Test
        void spec_marksEveryFieldRequiredExceptTheOptionalProblemFields() {
            var spec = assertThat(mvc.get().uri("/v3/api-docs")).hasStatusOk().bodyJson();
            spec.extractingPath("$.components.schemas.ProbeView.required")
                    .asArray()
                    .contains("name", "detail");
            spec.extractingPath("$.components.schemas.Problem.required")
                    .asArray()
                    .contains("code", "requestId")
                    .doesNotContain("invalidParams", "instance");
        }

        /** openapi-typescript reads anyOf, never a type written next to a $ref. */
        @Test
        void spec_writesNullableObjectAsAnyOfRefOrNull() {
            var spec = assertThat(mvc.get().uri("/v3/api-docs")).hasStatusOk().bodyJson();
            var detail = "$.components.schemas.ProbeView.properties.detail";
            spec.extractingPath(detail + ".anyOf").asArray().hasSize(2);
            spec.extractingPath(detail + ".anyOf[0].$ref")
                    .isEqualTo("#/components/schemas/ProbeDetail");
            spec.doesNotHavePath(detail + ".$ref");
        }
    }

    @TestConfiguration(proxyBeanMethods = false)
    @RestController
    static class ProbeApi {

        @GetMapping("/test/openapi-probe")
        UIApiResponse<ProbeView> probe() {
            return UIApiResponse.of(new ProbeView("probe", null));
        }
    }

    @Schema(name = "ProbeView")
    record ProbeView(
            String name, @Schema(types = {"object", "null"}) @Nullable ProbeDetail detail) {}

    @Schema(name = "ProbeDetail")
    record ProbeDetail(String text) {}
}
