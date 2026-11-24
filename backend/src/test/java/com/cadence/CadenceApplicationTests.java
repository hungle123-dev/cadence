package com.cadence;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
class CadenceApplicationTests {

    @Autowired
    Flyway flyway;

    @Test
    @DisplayName("TC-OPS-03 Migration Flyway thật chạy trên DB trống và Hibernate validate khớp schema")
    void contextLoads_withAllMigrationsApplied() {
        // The context only starts if Flyway migrated and Hibernate `validate` passed.
        assertThat(flyway.info().pending()).isEmpty();
        assertThat(flyway.info().applied()).isNotEmpty();
    }
}
