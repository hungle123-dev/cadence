package com.cadence;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.DynamicPropertyRegistrar;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;

/**
 * Real PostgreSQL for integration tests (test plan T3: never H2), same major version as production.
 * Public so integration tests in every module can import it.
 *
 * <p>The container runs the same {@code deploy/postgres/init.sh} as every other environment (not a
 * copy), so tests use the two roles of database doc section 6: Flyway migrates as {@code
 * cadence_owner}, the application reads and writes as {@code cadence_app}. A migration that forgets
 * to grant its schema to the application therefore fails here, not in production.
 *
 * <p>Spring caches the test context, so test classes with the same configuration share one
 * container.
 */
@TestConfiguration(proxyBeanMethods = false)
public class TestcontainersConfiguration {

    private static final String PASSWORD = "test";

    @Bean
    PostgreSQLContainer postgresContainer() {
        return new PostgreSQLContainer(DockerImageName.parse("postgres:17-alpine"))
                .withCopyFileToContainer(
                        MountableFile.forHostPath("../deploy/postgres/init.sh", 0755),
                        "/docker-entrypoint-initdb.d/init.sh")
                .withEnv("DB_OWNER_PASSWORD", PASSWORD)
                .withEnv("DB_APP_PASSWORD", PASSWORD);
    }

    /** init.sh creates the {@code cadence} database next to the container's default one. */
    @Bean
    DynamicPropertyRegistrar databaseProperties(PostgreSQLContainer postgres) {
        return registry -> {
            registry.add(
                    "spring.datasource.url",
                    () ->
                            "jdbc:postgresql://%s:%d/cadence"
                                    .formatted(
                                            postgres.getHost(),
                                            postgres.getMappedPort(
                                                    PostgreSQLContainer.POSTGRESQL_PORT)));
            registry.add("spring.datasource.username", () -> "cadence_app");
            registry.add("spring.datasource.password", () -> PASSWORD);
            registry.add("spring.flyway.user", () -> "cadence_owner");
            registry.add("spring.flyway.password", () -> PASSWORD);
        };
    }
}
