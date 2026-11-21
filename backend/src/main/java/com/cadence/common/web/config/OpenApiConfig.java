package com.cadence.common.web.config;

import java.util.List;
import java.util.Map;

import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.servers.Server;

/**
 * ADR-0006: the OpenAPI spec is generated from the controllers by springdoc (code-first).
 *
 * <p>Served at {@code /api/v3/api-docs}, Swagger UI at {@code /api/swagger-ui.html}; both are off
 * unless {@code SPRINGDOC_ENABLED=true} (on by default only in the {@code local} profile).
 *
 * <p>The server URL is relative, so "Try it out" works both on {@code localhost:8081} and behind
 * Nginx. No security scheme yet: login comes later and uses a session cookie, not JWT.
 */
@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Cadence API",
                version = "0.1.0",
                description = "REST API of the Cadence store. Errors are RFC 9457 Problem Details."),
        servers = @Server(url = "/api", description = "Same origin as the web app"))
public class OpenApiConfig {

    /**
     * Keys that are sometimes absent. Spring's ProblemDetail leaves out empty fields (type when it is
     * about:blank, instance, detail), and invalidParams only comes with invalid input.
     */
    private static final Map<String, List<String>> OPTIONAL_PROPERTIES =
            Map.of("Problem", List.of("type", "instance", "detail", "invalidParams"));

    /**
     * Marks every property of every schema as required. True to the wire format: Jackson writes every
     * record component, null included, so the key is always there. A value that may be null says so
     * with {@code @Schema(types = {"...", "null"})}. Without this the generated TypeScript types would
     * make every field optional.
     */
    @Bean
    OpenApiCustomizer allPropertiesRequired() {
        return openApi -> {
            if (openApi.getComponents() == null || openApi.getComponents().getSchemas() == null) {
                return;
            }
            openApi.getComponents().getSchemas().forEach((name, schema) -> {
                if (schema.getProperties() == null) {
                    return;
                }
                List<String> optional = OPTIONAL_PROPERTIES.getOrDefault(name, List.of());
                schema.setRequired(schema.getProperties().keySet().stream()
                        .filter(property -> !optional.contains(property))
                        .toList());
            });
        };
    }
}
