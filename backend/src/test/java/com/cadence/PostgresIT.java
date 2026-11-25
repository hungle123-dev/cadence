package com.cadence;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

/**
 * Base of every integration test: the whole application against a real PostgreSQL with the real
 * Flyway migrations, never H2 (test plan T3). An in-memory database misses exactly what these tests
 * are for: native SQL naming a column that does not exist, a wrong join, a missing constraint or
 * grant.
 *
 * <p>A subclass adds what it needs: {@code @AutoConfigureMockMvc}, {@code @Sql} seed data, {@code
 * Transactional} rollback, or its own {@code @SpringBootTest(properties = ...)}. Spring reuses a
 * context (and its container) only between tests with exactly the same configuration, so each extra
 * annotation or property costs one more context and one more PostgreSQL start: add one only when
 * the test needs it.
 */
@SpringBootTest
@Import(TestcontainersConfiguration.class)
public abstract class PostgresIT {}
