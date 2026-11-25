package com.cadence.architecture;

import static com.tngtech.archunit.base.DescribedPredicate.not;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAnyPackage;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.properties.HasName;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.CompositeArchRule;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import com.tngtech.archunit.library.dependencies.SlicesRuleDefinition;

/**
 * Architecture rules R1–R9 (architecture 7.7), module boundaries (ADR-0001, architecture 7.4) and
 * one schema per module (database P1). Test plan TC-ARC-01…04.
 *
 * <p>Some modules have no classes yet, so {@code allowEmptyShould(true)} keeps their rules green
 * until the first classes appear.
 *
 * <p>Not checked here: one aggregate per transaction cannot be verified statically; schemas named
 * in SQL are read from source text, not bytecode, by {@link NativeSqlSchemaTest} (SQL in Java) and
 * {@link MigrationSchemaTest} (migration folders).
 */
@AnalyzeClasses(packages = "com.cadence", importOptions = ImportOption.DoNotIncludeTests.class)
class ArchitectureTest {

    // ArchUnit reads the static @ArchTest fields; it never needs an instance.
    private ArchitectureTest() {}

    private static final String ROOT = "com.cadence";

    /** Business modules (architecture 7.2) and the schema each one owns (database P1). */
    static final Map<String, String> MODULE_SCHEMAS =
            Map.of(
                    "account", "account",
                    "catalog", "catalog",
                    "inventory", "inventory",
                    "ordering", "ordering",
                    "payment", "payment",
                    "notification", "notification");

    private static final List<String> MODULES = List.copyOf(MODULE_SCHEMAS.keySet());

    /**
     * Lombok is allowed in the domain. Its annotations vanish at compile time except the {@code
     * @lombok.Generated} marker (lombok.config), which has no behaviour: allow exactly that class,
     * not the whole lombok package.
     */
    private static final DescribedPredicate<JavaClass> LOMBOK_GENERATED =
            HasName.Predicates.name("lombok.Generated").forSubtype();

    // ---------- Layers inside a module ----------

    @ArchTest
    static final ArchRule R1_DOMAIN_DOES_NOT_DEPEND_ON_OUTER_LAYERS =
            noClasses()
                    .that()
                    .resideInAPackage(ROOT + "..domain..")
                    .should()
                    .dependOnClassesThat()
                    .resideInAnyPackage(ROOT + "..application..", ROOT + "..infrastructure..")
                    .because("R1: outer layers depend on the domain, never the reverse")
                    .allowEmptyShould(true);

    @ArchTest
    static final ArchRule R2_R4_DOMAIN_IS_PURE_AND_KNOWS_NO_OTHER_CONTEXT =
            forEachModule(
                    "R2, R4: a module's domain only uses the JDK, common.domain and its own domain",
                    name ->
                            classes()
                                    .that()
                                    .resideInAPackage(module(name) + ".domain..")
                                    .should()
                                    .onlyDependOnClassesThat(
                                            resideInAnyPackage(
                                                            "java..",
                                                            ROOT + ".common.domain..",
                                                            module(name) + ".domain..")
                                                    .or(LOMBOK_GENERATED))
                                    .because(
                                            "R2: no framework or ORM in the domain; R4: a domain"
                                                    + " does not know other contexts"));

    @ArchTest
    static final ArchRule R3_APPLICATION_DOES_NOT_DEPEND_ON_INFRASTRUCTURE =
            noClasses()
                    .that()
                    .resideInAPackage(ROOT + "..application..")
                    .should()
                    .dependOnClassesThat()
                    .resideInAPackage(ROOT + "..infrastructure..")
                    .because("R3: use cases reach the outside world only through their own ports")
                    .allowEmptyShould(true);

    @ArchTest
    static final ArchRule R7_CONTROLLERS_LIVE_IN_RESTAPI =
            classes()
                    .that()
                    .areAnnotatedWith("org.springframework.web.bind.annotation.RestController")
                    .or()
                    .areAnnotatedWith("org.springframework.stereotype.Controller")
                    .should()
                    .resideInAPackage("..infrastructure.restapi..")
                    .because("R7: controllers are driving adapters")
                    .allowEmptyShould(true);

    @ArchTest
    static final ArchRule R9_APPLICATION_DOES_NOT_USE_PERSISTENCE_TYPES =
            noClasses()
                    .that()
                    .resideInAPackage(ROOT + "..application..")
                    .should()
                    .dependOnClassesThat()
                    .resideInAnyPackage(
                            "jakarta.persistence..",
                            "org.hibernate..",
                            "org.springframework.data..")
                    .because(
                            "R9: ports never expose persistence types; adapters convert to domain"
                                    + " objects or DTOs")
                    .allowEmptyShould(true);

    // ---------- Between modules (ADR-0001, architecture 7.4) ----------

    @ArchTest
    static final ArchRule R5_APPLICATION_DOES_NOT_REACH_INTO_OTHER_CONTEXTS =
            forEachModule(
                    "R5: a module's application layer does not depend on other modules",
                    name ->
                            noClasses()
                                    .that()
                                    .resideInAPackage(module(name) + ".application..")
                                    .should()
                                    .dependOnClassesThat(resideInAnyPackage(otherModules(name)))
                                    .because(
                                            "R5: a use case talks to another context through its"
                                                    + " own port; the ACL adapter lives in"
                                                    + " infrastructure"));

    @ArchTest
    static final ArchRule R5_R6_INFRASTRUCTURE_USES_ONLY_PUBLIC_PARTS_OF_OTHER_CONTEXTS =
            forEachModule(
                    "R5, R6: infrastructure uses other modules only through their application layer"
                            + " or domain events",
                    name ->
                            noClasses()
                                    .that()
                                    .resideInAPackage(module(name) + ".infrastructure..")
                                    .should()
                                    .dependOnClassesThat(
                                            resideInAnyPackage(otherInternals(name))
                                                    .and(
                                                            not(
                                                                    resideInAnyPackage(
                                                                            otherEvents(name)))))
                                    .because(
                                            "R5: other contexts are reached through their"
                                                + " application layer; R6: their domain events may"
                                                + " be listened to"));

    @ArchTest
    static final ArchRule R8_COMMON_DOES_NOT_DEPEND_ON_BUSINESS_MODULES =
            noClasses()
                    .that()
                    .resideInAPackage(ROOT + ".common..")
                    .should()
                    .dependOnClassesThat()
                    .resideInAnyPackage(modulePackages(MODULES))
                    .because("R8: the shared kernel is below every module")
                    .allowEmptyShould(true);

    @ArchTest
    static final ArchRule R8_SHARED_KERNEL_IS_PURE =
            classes()
                    .that()
                    .resideInAnyPackage(ROOT + ".common.domain..", ROOT + ".common.application..")
                    .should()
                    .onlyDependOnClassesThat(
                            resideInAnyPackage(
                                            "java..",
                                            ROOT + ".common.domain..",
                                            ROOT + ".common.application..")
                                    .or(LOMBOK_GENERATED))
                    .because(
                            "R8: common/domain and common/application contain no framework code,"
                                    + " so every module's domain and use cases can use them")
                    .allowEmptyShould(true);

    // ---------- Package structure ----------

    @ArchTest
    static final ArchRule NO_CYCLES_BETWEEN_PACKAGES =
            SlicesRuleDefinition.slices()
                    .matching(ROOT + ".(**)")
                    .should()
                    .beFreeOfCycles()
                    .because(
                            "a cycle ties packages together: neither can change, move or be"
                                    + " tested without the other");

    // ---------- Persistence ----------

    @ArchTest
    static final ArchRule JPA_ONLY_IN_PERSISTENCE_ADAPTERS =
            noClasses()
                    .that()
                    .resideOutsideOfPackage(ROOT + "..infrastructure.persistence..")
                    .should()
                    .dependOnClassesThat()
                    .resideInAnyPackage("jakarta.persistence..", "org.hibernate..")
                    .because("JPA entities and repositories live in infrastructure/persistence")
                    .allowEmptyShould(true);

    @ArchTest
    static final ArchRule ENTITIES_MAP_TO_THEIR_MODULE_SCHEMA =
            forEachModule(
                    "Database P1: every JPA entity declares @Table(schema = <its module's schema>)",
                    name ->
                            classes()
                                    .that()
                                    .areAnnotatedWith("jakarta.persistence.Entity")
                                    .and()
                                    .resideInAPackage(module(name) + "..")
                                    .should(declareTableSchema(MODULE_SCHEMAS.get(name)))
                                    .because(
                                            "database P1: a module only reads and writes its own"
                                                    + " schema"));

    // ---------- API documentation ----------

    @ArchTest
    static final ArchRule OPENAPI_ANNOTATIONS_ONLY_IN_WEB_ADAPTERS =
            noClasses()
                    .that()
                    .resideOutsideOfPackages(
                            ROOT + "..infrastructure.restapi..", ROOT + ".common.web..")
                    .should()
                    .dependOnClassesThat()
                    .resideInAnyPackage("io.swagger..", "org.springdoc..")
                    .because(
                            "OpenAPI is a detail of the REST adapter; domain and use"
                                    + " cases stay framework-free, so request/response records that"
                                    + " carry @Schema live in infrastructure/restapi")
                    .allowEmptyShould(true);

    // ---------- Helpers ----------

    private static String module(String name) {
        return ROOT + "." + name;
    }

    /** Builds the same rule for every module and reports it under one short description. */
    private static ArchRule forEachModule(String description, Function<String, ArchRule> ruleFor) {
        List<ArchRule> rules =
                MODULES.stream().map(name -> ruleFor.apply(name).allowEmptyShould(true)).toList();
        return CompositeArchRule.of(rules).as(description).allowEmptyShould(true);
    }

    private static String[] modulePackages(List<String> modules) {
        return modules.stream().map(name -> module(name) + "..").toArray(String[]::new);
    }

    private static String[] otherModules(String self) {
        return modulePackages(MODULES.stream().filter(name -> !name.equals(self)).toList());
    }

    private static String[] otherInternals(String self) {
        return MODULES.stream()
                .filter(name -> !name.equals(self))
                .flatMap(name ->
                        java.util.stream.Stream.of(
                                module(name) + ".domain..", module(name) + ".infrastructure.."))
                .toArray(String[]::new);
    }

    private static String[] otherEvents(String self) {
        return MODULES.stream()
                .filter(name -> !name.equals(self))
                .map(name -> module(name) + ".domain.event..")
                .toArray(String[]::new);
    }

    private static ArchCondition<JavaClass> declareTableSchema(String schema) {
        return new ArchCondition<>("declare @Table(schema = \"" + schema + "\")") {
            @Override
            public void check(JavaClass entity, ConditionEvents events) {
                String actual =
                        entity.tryGetAnnotationOfType("jakarta.persistence.Table")
                                .flatMap(table -> table.get("schema"))
                                .map(Object::toString)
                                .orElse("");
                if (!schema.equals(actual)) {
                    events.add(
                            SimpleConditionEvent.violated(
                                    entity,
                                    "%s maps to schema '%s' instead of '%s'"
                                            .formatted(entity.getName(), actual, schema)));
                }
            }
        };
    }
}
