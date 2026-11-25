package com.cadence.common.web.validation;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class EnumValidatorTest {

    private static ValidatorFactory factory;
    private static Validator validator;

    enum Colour {
        BLACK,
        WHITE
    }

    record Filter(@ValidEnum(enumClass = Colour.class) String colour) {}

    @BeforeAll
    static void createValidator() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void closeFactory() {
        factory.close();
    }

    @Test
    void isValid_acceptsAnExactConstantName() {
        assertThat(validator.validate(new Filter("BLACK"))).isEmpty();
    }

    @Test
    void isValid_acceptsAbsentOrBlankBecauseAFilterNotSentIsNotWrong() {
        assertThat(validator.validate(new Filter(null))).isEmpty();
        assertThat(validator.validate(new Filter(" "))).isEmpty();
    }

    @Test
    void isValid_rejectsUnknownAndWrongCaseValues() {
        Set<ConstraintViolation<Filter>> unknown = validator.validate(new Filter("RED"));
        Set<ConstraintViolation<Filter>> lowerCase = validator.validate(new Filter("black"));

        assertThat(unknown)
                .singleElement()
                .extracting(ConstraintViolation::getMessage)
                .isEqualTo("Giá trị không nằm trong danh sách cho phép");
        assertThat(lowerCase).hasSize(1);
    }
}
