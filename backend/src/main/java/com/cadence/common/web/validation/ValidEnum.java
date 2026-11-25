package com.cadence.common.web.validation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

/**
 * The text must be the exact name of one constant of {@link #enumClass()}. Put it on a {@code
 * String} field of a request record, so a wrong value comes back as one entry of {@code
 * invalidParams} together with every other wrong field, instead of a conversion error.
 *
 * <p>Absent or blank is valid: a filter that is not sent is not wrong. Add {@code @NotBlank} when
 * the value is required. Matching is case sensitive, like the API contract ({@code BLACK}, not
 * {@code black}).
 */
@Documented
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.TYPE_USE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = EnumValidator.class)
public @interface ValidEnum {

    Class<? extends Enum<?>> enumClass();

    String message() default "Giá trị không nằm trong danh sách cho phép";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
