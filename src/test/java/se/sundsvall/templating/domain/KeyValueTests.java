package se.sundsvall.templating.domain;

import static java.util.List.copyOf;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.stream.Stream;

import jakarta.validation.Validation;
import jakarta.validation.Validator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class KeyValueTests {

    private Validator validator;

    @BeforeEach
    void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    void test_of_createsKeyValue_ok() {
        var keyValue = KeyValue.of("someKey", "someValue");

        assertThat(keyValue).isNotNull();
        assertThat(keyValue.getKey()).isEqualTo("someKey");
        assertThat(keyValue.getValue()).isEqualTo("someValue");
    }

    @ParameterizedTest
    @MethodSource("getKeyValuesForValidation")
    void test_validation(final KeyValue keyValue) {
        var constraints = copyOf(validator.validate(keyValue));

        assertThat(constraints).hasSize(1);
        assertThat(constraints.get(0)).satisfies(constraintViolation -> {
            if (isBlank(keyValue.getKey())) {
                assertThat(constraintViolation.getPropertyPath()).hasToString("key");
            } else {
                assertThat(constraintViolation.getPropertyPath()).hasToString("value");
            }
            assertThat(constraintViolation.getMessage()).isEqualTo("must not be blank");
        });
    }

    @Test
    void test_toString() {
        var keyValue = KeyValue.of("someKey", "someValue");

        assertThat(keyValue).hasToString("{someKey=someValue}");
    }

    static Stream<KeyValue> getKeyValuesForValidation() {
        return Stream.of(
            KeyValue.of(null, "someValue"),
            KeyValue.of("", "someValue"),
            KeyValue.of("someKey", null),
            KeyValue.of("someKey", "")
        );
    }
}
