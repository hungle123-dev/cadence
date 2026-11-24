package com.cadence.support;

import org.springframework.jdbc.core.simple.JdbcClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Runs the reconciliation queries of test plan 5.1.4 after concurrency, background-job and E2E tests.
 * Each query must return zero rows. Modules register their invariant when their tables exist (M1, M3…).
 */
public class InvariantChecker {

    /** name → SQL that returns the rows breaking the invariant. */
    private final Map<String, String> invariants;
    private final JdbcClient jdbc;

    public InvariantChecker(JdbcClient jdbc, Map<String, String> invariants) {
        this.jdbc = jdbc;
        this.invariants = Map.copyOf(invariants);
    }

    public void assertAllHold() {
        List<String> broken = new ArrayList<>();
        invariants.forEach((name, sql) -> {
            List<Map<String, Object>> rows = jdbc.sql(sql).query().listOfRows();
            if (!rows.isEmpty()) broken.add(name + " → " + rows);
        });
        assertThat(broken).as("database invariants").isEmpty();
    }
}
