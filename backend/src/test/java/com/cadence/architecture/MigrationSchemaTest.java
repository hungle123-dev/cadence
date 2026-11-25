package com.cadence.architecture;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

/**
 * Database P1 (test plan TC-ARC-04): a migration in db/migration/<module>/ only touches that
 * module's schema. The {@code common} folder holds the baseline and may name no schema at all.
 */
class MigrationSchemaTest {

    private static final Path MIGRATIONS = Path.of("src/main/resources/db/migration");

    private static final Map<String, String> MODULE_SCHEMAS = ArchitectureTest.MODULE_SCHEMAS;

    private static final String COMMON = "common";

    // "SCHEMA catalog", "SCHEMA IF NOT EXISTS catalog", or a qualified name such as
    // "catalog.categories"
    private static final Pattern SCHEMA_REF =
            Pattern.compile(
                    "(?i)\\bschema\\s+(?:if\\s+not\\s+exists\\s+)?([a-z_][a-z0-9_]*)|\\b([a-z_][a-z0-9_]*)\\.[a-z_]");

    @Test
    void migrations_touchOnlyTheirModuleSchema() throws IOException {
        List<String> problems = new ArrayList<>();
        try (Stream<Path> files = Files.walk(MIGRATIONS)) {
            for (Path file : files.filter(path -> path.toString().endsWith(".sql")).toList()) {
                String folder = MIGRATIONS.relativize(file).getName(0).toString();
                String expected = COMMON.equals(folder) ? "" : MODULE_SCHEMAS.get(folder);
                if (expected == null) {
                    problems.add(file + ": must live in a module folder (db/migration/<module>/)");
                    continue;
                }
                String sql = Files.readString(file).replaceAll("--[^\\n]*", "");
                Matcher m = SCHEMA_REF.matcher(sql);
                while (m.find()) {
                    String schema =
                            (m.group(1) != null ? m.group(1) : m.group(2)).toLowerCase(Locale.ROOT);
                    if (!schema.equals(expected)) {
                        problems.add(
                                file.getFileName()
                                        + ": uses schema '"
                                        + schema
                                        + "', expected '"
                                        + expected
                                        + "'");
                    }
                }
            }
        }
        assertTrue(problems.isEmpty(), String.join("\n", problems));
    }
}
