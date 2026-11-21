package com.cadence;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;

/** Throwaway PostgreSQL 18 for tests. Public so integration tests in every module can import it. */
@TestConfiguration(proxyBeanMethods = false)
public class TestcontainersConfiguration {

    @Bean
    @ServiceConnection
    PostgreSQLContainer postgresContainer() {
        return new PostgreSQLContainer(DockerImageName.parse("postgres:18"))
                // Tạo 2 tài khoản giống hệt máy cá nhân (ADR-0014): cùng một script, không viết lại
                .withCopyFileToContainer(
                        MountableFile.forHostPath("../infra/db/init/01-roles.sh"),
                        "/docker-entrypoint-initdb.d/01-roles.sh")
                .withEnv("CADENCE_MIGRATOR_PASSWORD", "test")
                .withEnv("CADENCE_APP_PASSWORD", "test");
    }

}
