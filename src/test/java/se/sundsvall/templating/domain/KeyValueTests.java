package se.sundsvall.templating.domain;

import static java.util.List.copyOf;
import static org.assertj.core.api.Assertions.assertThat;

import javax.validation.Validation;
import javax.validation.Validator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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

    @Test
    void test_validationWithNullKey_fails() {
        var keyValue = KeyValue.of(null, "someValue");

        var constraints = copyOf(validator.validate(keyValue));

        assertThat(constraints).hasSize(1);
        assertThat(constraints.get(0)).satisfies(constraintViolation -> {
            assertThat(constraintViolation.getPropertyPath().toString()).isEqualTo("key");
            assertThat(constraintViolation.getMessage()).isEqualTo("must not be blank");
        });
    }

    @Test
    void test_validationWithEmptyKey_fails() {
        var keyValue = KeyValue.of("", "someValue");

        var constraints = copyOf(validator.validate(keyValue));

        assertThat(constraints).hasSize(1);
        assertThat(constraints.get(0)).satisfies(constraintViolation -> {
            assertThat(constraintViolation.getPropertyPath().toString()).isEqualTo("key");
            assertThat(constraintViolation.getMessage()).isEqualTo("must not be blank");
        });
    }

    @Test
    void test_validationWithNullValue_fails() {
        var keyValue = KeyValue.of("someKey", null);

        var constraints = copyOf(validator.validate(keyValue));

        assertThat(constraints).hasSize(1);
        assertThat(constraints.get(0)).satisfies(constraintViolation -> {
            assertThat(constraintViolation.getPropertyPath().toString()).isEqualTo("value");
            assertThat(constraintViolation.getMessage()).isEqualTo("must not be blank");
        });
    }

    @Test
    void test_validationWithEmptyValue_fails() {
        var keyValue = KeyValue.of("someKey", "");

        var constraints = copyOf(validator.validate(keyValue));

        assertThat(constraints).hasSize(1);
        assertThat(constraints.get(0)).satisfies(constraintViolation -> {
            assertThat(constraintViolation.getPropertyPath().toString()).isEqualTo("value");
            assertThat(constraintViolation.getMessage()).isEqualTo("must not be blank");
        });
    }

    @Test
    void test_toString() {
        var keyValue = KeyValue.of("someKey", "someValue");

        assertThat(keyValue.toString()).isEqualTo("{someKey=someValue}");
    }
}
