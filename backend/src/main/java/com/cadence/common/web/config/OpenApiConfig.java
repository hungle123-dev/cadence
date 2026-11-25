package com.cadence.common.web.config;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import com.cadence.common.web.ApiProblem;

import io.swagger.v3.core.converter.ModelConverters;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.JsonSchema;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;

/**
 * The OpenAPI spec is generated from the controllers by springdoc (code-first, SRS NFR-MNT-04).
 *
 * <p>Served at {@code /api/v3/api-docs}, Swagger UI at {@code /api/swagger-ui.html}; both are off
 * unless {@code API_DOCS_ENABLED=true} (on in the {@code dev} profile).
 *
 * <p>The server URL is relative, so "Try it out" works both on localhost and behind Nginx. No
 * security scheme yet: sign-in will use a session cookie, not JWT (ADR-0003).
 */
@Configuration
@OpenAPIDefinition(
        info =
                @Info(
                        title = "Cadence API",
                        version = "0.1.0",
                        description =
                                "REST API of the Cadence store. Errors are RFC 9457 Problem"
                                        + " Details."),
        servers = @Server(url = "/api", description = "Same origin as the web app"))
public class OpenApiConfig {

    /**
     * Keys that are sometimes absent. Spring's ProblemDetail leaves out empty fields (type when it
     * is about:blank, instance, detail), and invalidParams only comes with invalid input.
     */
    private static final Map<String, List<String>> OPTIONAL_PROPERTIES =
            Map.of("Problem", List.of("type", "instance", "detail", "invalidParams"));

    /** The errors every endpoint can answer; the {@code code} says which one it is. */
    private static final Map<String, String> PROBLEM_RESPONSES =
            Map.of(
                    "400",
                    "Invalid input: VALIDATION_ERROR, or a module code such as"
                            + " CATALOG_INVALID_FILTER; invalidParams lists the wrong fields",
                    "404",
                    "Not found: unknown path, or a module code such as CATALOG_PRODUCT_NOT_FOUND",
                    "500",
                    "INTERNAL_ERROR: details are in the log under the requestId");

    /**
     * Errors are documented once here, not with an {@code @ApiResponse} per error code on every
     * controller method. Runs first, so the Problem schema it registers is also marked required
     * below.
     */
    @Bean
    @Order(0)
    OpenApiCustomizer problemResponses() {
        return openApi -> {
            if (openApi.getComponents() == null) {
                openApi.setComponents(new Components());
            }
            ModelConverters.getInstance()
                    .readAll(ApiProblem.class)
                    .forEach(openApi.getComponents()::addSchemas);
            if (openApi.getPaths() == null) {
                return;
            }
            openApi.getPaths().values().stream()
                    .flatMap(path -> path.readOperations().stream())
                    .forEach(operation ->
                            PROBLEM_RESPONSES.forEach((status, description) ->
                                    operation
                                            .getResponses()
                                            .computeIfAbsent(
                                                    status, key -> problemResponse(description))));
        };
    }

    private static ApiResponse problemResponse(String description) {
        Schema<?> problem = new Schema<>().$ref("#/components/schemas/Problem");
        return new ApiResponse()
                .description(description)
                .content(
                        new Content()
                                .addMediaType(
                                        ApiProblem.MEDIA_TYPE, new MediaType().schema(problem)));
    }

    /**
     * Marks every property of every schema as required. True to the wire format: Jackson writes
     * every record component, null included, so the key is always there. A value that may be null
     * says so with {@code @Schema(types = {"...", "null"})}. Without this the generated TypeScript
     * types would make every field optional.
     */
    @Bean
    @Order(1)
    OpenApiCustomizer allPropertiesRequired() {
        return openApi -> {
            if (openApi.getComponents() == null || openApi.getComponents().getSchemas() == null) {
                return;
            }
            openApi.getComponents()
                    .getSchemas()
                    .forEach((name, schema) -> {
                        if (schema.getProperties() == null) {
                            return;
                        }
                        List<String> optional = OPTIONAL_PROPERTIES.getOrDefault(name, List.of());
                        schema.setRequired(
                                schema.getProperties().keySet().stream()
                                        .filter(property -> !optional.contains(property))
                                        .toList());
                    });
        };
    }

    /**
     * A nullable object field becomes anyOf [$ref, null]. springdoc writes the "null" type next to
     * the $ref, which OpenAPI 3.1 allows, but openapi-typescript ignores whatever sits next to a
     * $ref, so the generated type would lose "| null".
     */
    @Bean
    @Order(2)
    OpenApiCustomizer nullableRefsAsAnyOf() {
        return openApi -> {
            if (openApi.getComponents() == null || openApi.getComponents().getSchemas() == null) {
                return;
            }
            for (Schema<?> schema : openApi.getComponents().getSchemas().values()) {
                if (schema.getProperties() == null) {
                    continue;
                }
                schema.getProperties()
                        .replaceAll((name, property) ->
                                isNullableRef(property) ? anyOfRefOrNull(property) : property);
            }
        };
    }

    /** True for a property written as $ref plus a type that allows null. */
    private static boolean isNullableRef(Schema<?> property) {
        return property.get$ref() != null
                && property.getTypes() != null
                && property.getTypes().contains("null");
    }

    /** Same property as anyOf [the referenced schema, null], keeping its description. */
    private static Schema<?> anyOfRefOrNull(Schema<?> property) {
        return new JsonSchema()
                .description(property.getDescription())
                .anyOf(
                        List.of(
                                new JsonSchema().$ref(property.get$ref()),
                                new JsonSchema().types(Set.of("null"))));
    }
}
