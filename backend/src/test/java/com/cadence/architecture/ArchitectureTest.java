package com.cadence.architecture;

import static com.tngtech.archunit.base.DescribedPredicate.not;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAnyPackage;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.CompositeArchRule;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;

/**
 * Architecture rules: template R1–R9, ADR-0001 (module boundaries), ADR-0011 (JPA placement),
 * ADR-0014 (schema per module).
 *
 * <p>The rules are written before any business code exists, so most of them have no classes to
 * check yet. {@code allowEmptyShould(true)} keeps them green until the first classes appear.
 *
 * <p>Not checked here: R10 (one aggregate per transaction) cannot be verified statically; native SQL
 * and migration folders (ADR-0014) get their own test in S4; "no role checks" (ADR-0015) is added
 * together with Spring Security.
 */
@AnalyzeClasses(packages = "com.cadence", importOptions = ImportOption.DoNotIncludeTests.class)
class ArchitectureTest {

	private static final String ROOT = "com.cadence";

	/** Business modules and the database schema each one owns (step 5, section 5.2). */
	private static final Map<String, String> MODULE_SCHEMAS = Map.of(
			"catalog", "catalog",
			"cart", "cart",
			"inventory", "inventory",
			"order", "ordering", // "order" is an SQL keyword
			"payment", "payment",
			"fulfillment", "fulfillment",
			"identity", "identity",
			"notification", "notification");

	private static final List<String> MODULES = List.copyOf(MODULE_SCHEMAS.keySet());

	// ---------- Layers inside a module ----------

	@ArchTest
	static final ArchRule r1_domain_does_not_depend_on_outer_layers = noClasses()
			.that().resideInAPackage(ROOT + "..domain..")
			.should().dependOnClassesThat().resideInAnyPackage(ROOT + "..application..", ROOT + "..infrastructure..")
			.because("R1: outer layers depend on the domain, never the reverse")
			.allowEmptyShould(true);

	@ArchTest
	static final ArchRule r2_r4_domain_is_pure_and_knows_no_other_context = forEachModule(
			"R2, R4: a module's domain only uses the JDK, common.domain and its own domain", m -> classes()
			.that().resideInAPackage(module(m) + ".domain..")
			.should().onlyDependOnClassesThat().resideInAnyPackage("java..", ROOT + ".common.domain..", module(m) + ".domain..")
			.because("R2: no framework or ORM in the domain; R4: a domain does not know other contexts"));

	@ArchTest
	static final ArchRule r3_application_does_not_depend_on_infrastructure = noClasses()
			.that().resideInAPackage(ROOT + "..application..")
			.should().dependOnClassesThat().resideInAPackage(ROOT + "..infrastructure..")
			.because("R3: use cases reach the outside world only through their own ports")
			.allowEmptyShould(true);

	@ArchTest
	static final ArchRule r7_controllers_live_in_restapi = classes()
			.that().areAnnotatedWith("org.springframework.web.bind.annotation.RestController")
			.or().areAnnotatedWith("org.springframework.stereotype.Controller")
			.should().resideInAPackage("..infrastructure.restapi..")
			.because("R7: controllers are driving adapters")
			.allowEmptyShould(true);

	@ArchTest
	static final ArchRule r9_application_does_not_use_persistence_types = noClasses()
			.that().resideInAPackage(ROOT + "..application..")
			.should().dependOnClassesThat().resideInAnyPackage("jakarta.persistence..", "org.hibernate..", "org.springframework.data..")
			.because("R9: ports never expose persistence types; adapters convert to domain objects or DTOs")
			.allowEmptyShould(true);

	// ---------- Between modules (ADR-0001) ----------

	@ArchTest
	static final ArchRule r5_application_does_not_reach_into_other_contexts = forEachModule(
			"R5: a module's application layer does not depend on other modules", m -> noClasses()
			.that().resideInAPackage(module(m) + ".application..")
			.should().dependOnClassesThat(resideInAnyPackage(otherModules(m)))
			.because("R5: a use case talks to another context through its own port; the ACL adapter lives in infrastructure"));

	@ArchTest
	static final ArchRule r5_r6_infrastructure_uses_only_public_parts_of_other_contexts = forEachModule(
			"R5, R6: infrastructure uses other modules only through their application layer or domain events", m -> noClasses()
			.that().resideInAPackage(module(m) + ".infrastructure..")
			.should().dependOnClassesThat(resideInAnyPackage(otherInternals(m)).and(not(resideInAnyPackage(otherEvents(m)))))
			.because("R5: other contexts are reached through their application layer; R6: their domain events may be listened to"));

	@ArchTest
	static final ArchRule r8_common_does_not_depend_on_business_modules = noClasses()
			.that().resideInAPackage(ROOT + ".common..")
			.should().dependOnClassesThat().resideInAnyPackage(modulePackages(MODULES))
			.because("R8: the shared kernel is below every module")
			.allowEmptyShould(true);

	@ArchTest
	static final ArchRule r8_shared_kernel_is_pure = classes()
			.that().resideInAPackage(ROOT + ".common.domain..")
			.should().onlyDependOnClassesThat().resideInAnyPackage("java..", ROOT + ".common.domain..")
			.because("R8: common/domain contains no framework code")
			.allowEmptyShould(true);

	// ---------- Persistence ----------

	@ArchTest
	static final ArchRule jpa_only_in_persistence_adapters = noClasses()
			.that().resideOutsideOfPackage(ROOT + "..infrastructure.persistence..")
			.should().dependOnClassesThat().resideInAnyPackage("jakarta.persistence..", "org.hibernate..")
			.because("ADR-0011: JPA entities and repositories live in infrastructure/persistence")
			.allowEmptyShould(true);

	@ArchTest
	static final ArchRule entities_map_to_their_module_schema = forEachModule(
			"ADR-0014: every JPA entity declares @Table(schema = <its module's schema>)", m -> classes()
			.that().areAnnotatedWith("jakarta.persistence.Entity")
			.and().resideInAPackage(module(m) + "..")
			.should(declareTableSchema(MODULE_SCHEMAS.get(m)))
			.because("ADR-0014: a module only reads and writes its own schema"));

	// ---------- Helpers ----------

	private static String module(String name) {
		return ROOT + "." + name;
	}

	/** Builds the same rule for every module and reports it under one short description. */
	private static ArchRule forEachModule(String description, Function<String, ArchRule> ruleFor) {
		List<ArchRule> rules = MODULES.stream().map(m -> ruleFor.apply(m).allowEmptyShould(true)).toList();
		return CompositeArchRule.of(rules).as(description).allowEmptyShould(true);
	}

	private static String[] modulePackages(List<String> modules) {
		return modules.stream().map(m -> module(m) + "..").toArray(String[]::new);
	}

	private static String[] otherModules(String self) {
		return modulePackages(MODULES.stream().filter(m -> !m.equals(self)).toList());
	}

	private static String[] otherInternals(String self) {
		return MODULES.stream().filter(m -> !m.equals(self))
				.flatMap(m -> java.util.stream.Stream.of(module(m) + ".domain..", module(m) + ".infrastructure.."))
				.toArray(String[]::new);
	}

	private static String[] otherEvents(String self) {
		return MODULES.stream().filter(m -> !m.equals(self)).map(m -> module(m) + ".domain.event..").toArray(String[]::new);
	}

	private static ArchCondition<JavaClass> declareTableSchema(String schema) {
		return new ArchCondition<>("declare @Table(schema = \"" + schema + "\")") {
			@Override
			public void check(JavaClass entity, ConditionEvents events) {
				String actual = entity.tryGetAnnotationOfType("jakarta.persistence.Table")
						.flatMap(table -> table.get("schema"))
						.map(Object::toString)
						.orElse("");
				if (!schema.equals(actual)) {
					events.add(SimpleConditionEvent.violated(entity, "%s maps to schema '%s' instead of '%s'"
							.formatted(entity.getName(), actual, schema)));
				}
			}
		};
	}

}
