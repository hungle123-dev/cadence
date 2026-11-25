package com.cadence.catalog;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.test.context.ActiveProfiles;

import com.cadence.PostgresIT;

/** The demo runs on the sample data (SRS 7.8, database doc 7); production would not. */
@ActiveProfiles("demo")
class DemoProfileIntegrationTest extends PostgresIT {

    @Autowired private JdbcClient jdbc;

    @Test
    @DisplayName("NFR-DEMO-02 Profile demo nạp dữ liệu mẫu khi khởi động")
    void demoProfile_loadsTheSampleCatalog() {
        assertThat(jdbc.sql("SELECT count(*) FROM catalog.products").query(Long.class).single())
                .isEqualTo(42);
        assertThat(jdbc.sql("SELECT count(*) FROM catalog.categories").query(Long.class).single())
                .isEqualTo(18);
    }
}
