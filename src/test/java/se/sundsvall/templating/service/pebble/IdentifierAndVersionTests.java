package se.sundsvall.templating.service.pebble;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatNoException;

import java.util.Date;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class IdentifierAndVersionTests {

    @ParameterizedTest
    @ValueSource(strings = {"#some.identifier", "some.identifier:", "some.identifier:1", "some.identifier:x.y"})
    void testCreateWithInvalidInput(final String input) {
        assertThatExceptionOfType(IllegalArgumentException.class)
            .isThrownBy(() -> new IdentifierAndVersion(input));
    }

    @ParameterizedTest
    @ValueSource(strings = {"some.identifier", "some.identifier:1.0"})
    void testCreateWithValidInput(final String input) {
        assertThatNoException().isThrownBy(() -> new IdentifierAndVersion(input));
    }


    @Test
    void testEquals() {
        assertThat(new IdentifierAndVersion("something:1.0").equals(new Date())).isFalse();
        assertThat(new IdentifierAndVersion("something:1.0").equals(new IdentifierAndVersion("something:1.0"))).isTrue();
        assertThat(new IdentifierAndVersion("something:1.0").equals(new IdentifierAndVersion("something:1.1"))).isFalse();

        var v = new IdentifierAndVersion("something:1.0");

        assertThat(v.equals(v)).isTrue();
    }
}
