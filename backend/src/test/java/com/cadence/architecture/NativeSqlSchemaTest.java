package com.cadence.architecture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

/**
 * Database P1 (test plan TC-ARC-04): SQL written in Java (JdbcClient text,
 * {@code @Query(nativeQuery = true)}) names only its own module's schema. The application has one
 * database user that can read every schema, so this test is what stops the catalog from reading
 * inventory's tables. Migrations are checked by {@link MigrationSchemaTest}.
 *
 * <p>Reads the string literals and text blocks of src/main/java. Only known schema names count, so
 * an SQL alias ({@code p.status}) never matches.
 */
class NativeSqlSchemaTest {

    private static final Path SOURCES = Path.of("src/main/java/com/cadence");

    /** Java package of each business module and the schema it owns. */
    private static final Map<String, String> MODULE_SCHEMAS = ArchitectureTest.MODULE_SCHEMAS;

    /** Every module may name these. None today; kept so a shared schema is one line to add. */
    private static final Set<String> SHARED_SCHEMAS = Set.of();

    /** Schemas no module may name: Flyway's history and PostgreSQL's own. */
    private static final Set<String> OTHER_SCHEMAS =
            Set.of("flyway", "public", "pg_catalog", "information_schema");

    private static final Set<String> KNOWN_SCHEMAS = knownSchemas();

    /** A char literal such as '"' would otherwise look like the start of a string. */
    private static final Pattern CHAR_LITERAL = Pattern.compile("'(?:[^'\\\\\\n]|\\\\.)'");

    private static final Pattern TEXT_BLOCK = Pattern.compile("\"\"\"(.*?)\"\"\"", Pattern.DOTALL);

    private static final Pattern STRING_LITERAL = Pattern.compile("\"((?:[^\"\\\\\\n]|\\\\.)*)\"");

    /** "inventory.stock": a name, a dot, a letter. Not after a dot: "com.cadence.catalog.x". */
    private static final Pattern QUALIFIED_NAME =
            Pattern.compile("(?i)(?<![\\w.])([a-z_][a-z0-9_]*)\\.[a-z_]");

    @Test
    void nativeSql_namesOnlyItsModuleAndTheSharedSchemas() throws IOException {
        List<String> problems = new ArrayList<>();
        try (Stream<Path> files = Files.walk(SOURCES)) {
            for (Path file : files.filter(path -> path.toString().endsWith(".java")).toList()) {
                Set<String> allowed = allowedSchemasFor(file);
                for (String schema : schemasNamedIn(Files.readString(file))) {
                    if (!allowed.contains(schema)) {
                        problems.add(SOURCES.relativize(file) + ": names schema '" + schema + "'");
                    }
                }
            }
        }
        assertTrue(problems.isEmpty(), String.join("\n", problems));
    }

    /** Guards the scanner: a scanner that finds nothing would make the test above always pass. */
    @Test
    void schemasNamedIn_findsTablesInTextBlocksAndStrings() {
        String source =
                """
                String tiles = \"""
                    SELECT * FROM inventory.stock_items s
                    \""";
                String join = "JOIN catalog.variants v ON v.id = s.variant_id";
                """;

        assertEquals(Set.of("inventory", "catalog"), schemasNamedIn(source));
    }

    @Test
    void schemasNamedIn_ignoresAliasesAndJavaCode() {
        String source =
                """
                import com.cadence.inventory.domain.StockItem;
                char quote = '"';
                String sql = "SELECT p.status, c.id FROM products p WHERE p.slug = :slug";
                String key = "com.cadence.inventory.Stock";
                """;

        assertEquals(Set.of(), schemasNamedIn(source));
    }

    @Test
    void allowedSchemasFor_givesAModuleItsOwnSchemaAndCommonOnlyTheSharedOnes() {
        assertEquals(
                Set.of("ordering"),
                allowedSchemasFor(SOURCES.resolve("ordering/infrastructure/adapter/X.java")));
        assertEquals(
                SHARED_SCHEMAS,
                allowedSchemasFor(SOURCES.resolve("common/web/GlobalExceptionHandler.java")));
    }

    private static Set<String> allowedSchemasFor(Path file) {
        Set<String> allowed = new HashSet<>(SHARED_SCHEMAS);
        String own = MODULE_SCHEMAS.get(SOURCES.relativize(file).getName(0).toString());
        if (own != null) {
            allowed.add(own);
        }
        return allowed;
    }

    /** Known schemas named in the string literals and text blocks of one Java source file. */
    private static Set<String> schemasNamedIn(String source) {
        List<String> texts = new ArrayList<>();
        String rest = CHAR_LITERAL.matcher(source).replaceAll("");
        Matcher block = TEXT_BLOCK.matcher(rest);
        while (block.find()) {
            texts.add(block.group(1));
        }
        Matcher string = STRING_LITERAL.matcher(block.replaceAll(""));
        while (string.find()) {
            texts.add(string.group(1));
        }

        Set<String> schemas = new HashSet<>();
        for (String text : texts) {
            Matcher name = QUALIFIED_NAME.matcher(text);
            while (name.find()) {
                String candidate = name.group(1).toLowerCase(Locale.ROOT);
                if (KNOWN_SCHEMAS.contains(candidate)) {
                    schemas.add(candidate);
                }
            }
        }
        return schemas;
    }

    private static Set<String> knownSchemas() {
        Set<String> known = new HashSet<>(MODULE_SCHEMAS.values());
        known.addAll(SHARED_SCHEMAS);
        known.addAll(OTHER_SCHEMAS);
        return Set.copyOf(known);
    }
}
