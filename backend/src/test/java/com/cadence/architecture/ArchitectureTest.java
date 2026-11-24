package com.cadence.architecture;

import com.tngtech.archunit.core.domain.Dependency;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Dependency rules R1–R7 of the backend template, plus "no cycles between modules" (architecture 7.7).
 */
@AnalyzeClasses(packages = "com.cadence", importOptions = ImportOption.DoNotIncludeTests.class)
class ArchitectureTest {

    private static final String ROOT = "com.cadence.";
    private static final Pattern MODULE = Pattern.compile("^com\\.cadence\\.([a-z]+)\\.(domain|application|infrastructure)\\b.*");

    // ---------- TC-ARC-01: layers inside a module ----------

    @ArchTest
    // TC-ARC-01 R1: domain không phụ thuộc application, infrastructure
    static final ArchRule domainDoesNotDependOnOuterLayers =
            noClasses().that().resideInAPackage("..domain..")
                    .should().dependOnClassesThat().resideInAnyPackage("..application..", "..infrastructure..");

    @ArchTest
    // TC-ARC-01 R2: domain không dùng Spring, JPA, Hibernate
    static final ArchRule domainIsFrameworkFree =
            noClasses().that().resideInAPackage("..domain..")
                    .should().dependOnClassesThat()
                    .resideInAnyPackage("org.springframework..", "jakarta.persistence..", "org.hibernate..");

    @ArchTest
    // TC-ARC-01 R3: application không phụ thuộc infrastructure
    static final ArchRule applicationDoesNotDependOnInfrastructure =
            noClasses().that().resideInAPackage("..application..")
                    .should().dependOnClassesThat().resideInAPackage("..infrastructure..");

    @ArchTest
    // TC-ARC-01 R4: application không dùng kiểu JPA, Hibernate, Spring Data
    static final ArchRule applicationDoesNotUsePersistenceTypes =
            noClasses().that().resideInAPackage("..application..")
                    .should().dependOnClassesThat()
                    .resideInAnyPackage("jakarta.persistence..", "org.hibernate..", "org.springframework.data..");

    @ArchTest
    // TC-ARC-01 R6: controller chỉ nằm ở infrastructure.restapi
    static final ArchRule controllersLiveInRestApi =
            classes().that().areAnnotatedWith(org.springframework.web.bind.annotation.RestController.class)
                    .should().resideInAPackage("..infrastructure.restapi..")
                    .orShould().resideInAPackage("com.cadence.common.web..")
                    .allowEmptyShould(true);

    @ArchTest
    // TC-ARC-01 R7: common không phụ thuộc module nghiệp vụ
    static final ArchRule commonDoesNotDependOnModules =
            classes().that().resideInAPackage("com.cadence.common..")
                    .should(onlyDependOnCommonInsideTheApplication());

    // ---------- TC-ARC-02: between modules ----------

    @ArchTest
    // TC-ARC-02 R5: module chỉ gọi module khác qua use case công khai, từ infrastructure
    static final ArchRule modulesTalkOnlyThroughPublicUseCases =
            classes().that().resideInAPackage("com.cadence.(*)..")
                    .and().resideOutsideOfPackage("com.cadence.common..")
                    .should(respectModuleBoundaries())
                    .allowEmptyShould(true);

    // ---------- TC-ARC-03: no cycles ----------

    @ArchTest
    // TC-ARC-03 Không có vòng phụ thuộc giữa các module
    static final ArchRule noCyclesBetweenModules =
            slices().matching("com.cadence.(*)..").should().beFreeOfCycles();

    // ---------- TC-ARC-04: SQL stays inside its own schema ----------

    private static final List<String> SCHEMAS =
            List.of("account", "catalog", "inventory", "ordering", "payment", "notification");

    @Test
    @DisplayName("TC-ARC-04 Migration của một module không đọc, ghi schema của module khác")
    void migrations_touchOnlyTheirOwnSchema() throws IOException {
        Path root = Path.of("src/main/resources/db/migration");
        List<String> violations = new ArrayList<>();
        try (Stream<Path> files = Files.walk(root)) {
            for (Path file : files.filter(p -> p.toString().endsWith(".sql")).toList()) {
                String module = root.relativize(file).getName(0).toString();
                String sql = Files.readString(file);
                for (String schema : SCHEMAS) {
                    if (schema.equals(module)) continue;
                    Matcher m = Pattern.compile("\\b" + schema + "\\.[a-z_]+").matcher(sql);
                    if (m.find()) violations.add(file + " references " + m.group());
                }
            }
        }
        assertThat(violations).isEmpty();
    }

    // ---------- conditions ----------

    private static ArchCondition<JavaClass> onlyDependOnCommonInsideTheApplication() {
        return new ArchCondition<>("only depend on com.cadence.common inside the application") {
            @Override
            public void check(JavaClass origin, ConditionEvents events) {
                for (Dependency d : origin.getDirectDependenciesFromSelf()) {
                    String target = d.getTargetClass().getPackageName();
                    if (target.startsWith(ROOT) && !target.startsWith(ROOT + "common")) {
                        events.add(SimpleConditionEvent.violated(d, d.getDescription()));
                    }
                }
            }
        };
    }

    /**
     * Template R5: module A must not import B's domain or application, except that A's infrastructure
     * (its adapters) may call B's public use cases (application.port.driving), use B's DTOs
     * (application.dto) and listen to B's events (domain.event).
     */
    private static ArchCondition<JavaClass> respectModuleBoundaries() {
        return new ArchCondition<>("respect module boundaries (template R5)") {
            @Override
            public void check(JavaClass origin, ConditionEvents events) {
                Optional<String[]> from = moduleAndLayer(origin.getPackageName());
                if (from.isEmpty()) return;
                for (Dependency d : origin.getDirectDependenciesFromSelf()) {
                    String targetPackage = d.getTargetClass().getPackageName();
                    Optional<String[]> to = moduleAndLayer(targetPackage);
                    if (to.isEmpty() || to.get()[0].equals(from.get()[0])) continue;

                    String otherModule = ROOT + to.get()[0] + ".";
                    boolean allowed = from.get()[1].equals("infrastructure")
                            && (targetPackage.startsWith(otherModule + "application.port.driving")
                                || targetPackage.startsWith(otherModule + "application.dto")
                                || targetPackage.startsWith(otherModule + "domain.event"));
                    if (!allowed) events.add(SimpleConditionEvent.violated(d, d.getDescription()));
                }
            }
        };
    }

    private static Optional<String[]> moduleAndLayer(String packageName) {
        Matcher m = MODULE.matcher(packageName);
        if (!m.matches() || m.group(1).equals("common")) return Optional.empty();
        return Optional.of(new String[]{m.group(1), m.group(2)});
    }
}
