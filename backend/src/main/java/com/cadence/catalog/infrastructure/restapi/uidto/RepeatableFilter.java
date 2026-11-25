package com.cadence.catalog.infrastructure.restapi.uidto;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.ReportAsSingleViolation;
import jakarta.validation.constraints.Size;

/**
 * A repeatable list filter takes at most 30 values. No filter needs more (18 categories, 18 sizes),
 * and a request repeating a value thousands of times must not turn into a huge query.
 */
@Documented
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.RECORD_COMPONENT})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = {})
@Size(max = 30) @ReportAsSingleViolation
public @interface RepeatableFilter {

    String message() default "Tối đa 30 giá trị";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
