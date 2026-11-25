package com.cadence.common.web.validation;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import org.jspecify.annotations.Nullable;

/** Checks {@link ValidEnum}: the names are read once, when the validator is created. */
public class EnumValidator implements ConstraintValidator<ValidEnum, String> {

    private Set<String> names = Set.of();

    @Override
    public void initialize(ValidEnum annotation) {
        names =
                Arrays.stream(annotation.enumClass().getEnumConstants())
                        .map(Enum::name)
                        .collect(Collectors.toUnmodifiableSet());
    }

    @Override
    public boolean isValid(@Nullable String value, ConstraintValidatorContext context) {
        return value == null || value.isBlank() || names.contains(value);
    }
}
