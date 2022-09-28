package se.sundsvall.templating.api.domain.validation;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class ValidTemplateIdValidatorTests {

    private final ValidTemplateIdValidator validator = new ValidTemplateIdValidator();

    @Test
    void test_nullInput_isNotValid() {
        assertThat(validator.isValid(null, null)).isFalse();
    }

    @Test
    void test_invalidInput_isNotValid() {
        assertThat(validator.isValid("#someInvalidValue", null)).isFalse();
    }

    @ParameterizedTest
    @ValueSource(strings = {"lower", "upper", "with.dot", "with-dash", "number-123", "aaa-123.BBB"})
    void test_validInput_isValid(final String s) {
        assertThat(validator.isValid(s, null)).isTrue();
    }
}
