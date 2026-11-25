package com.cadence;

import static org.assertj.core.api.Assertions.assertThat;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.simple.JdbcClient;

class CadenceApplicationTests extends PostgresIT {

    @Autowired private Flyway flyway;

    @Autowired private JdbcClient jdbc;

    @Test
    @DisplayName(
            "TC-OPS-03 Migration Flyway thật chạy trên DB trống và Hibernate validate khớp schema")
    void contextLoads_withAllMigrationsApplied() {
        // The context only starts if Flyway migrated and Hibernate `validate` passed.
        assertThat(flyway.info().pending()).isEmpty();
        assertThat(flyway.info().applied()).isNotEmpty();
    }

    @Test
    @DisplayName("Database mục 6: ứng dụng chạy bằng cadence_app, không phải role sở hữu schema")
    void application_connectsWithTheDataOnlyRole() {
        assertThat(jdbc.sql("SELECT current_user").query(String.class).single())
                .isEqualTo("cadence_app");
    }
}
