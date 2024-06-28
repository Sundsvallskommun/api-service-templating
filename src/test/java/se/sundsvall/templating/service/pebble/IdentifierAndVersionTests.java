package se.sundsvall.templating.service.pebble;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatNoException;

import java.util.Date;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class IdentifierAndVersionTests {

    private static final String MUNICIPALITY_ID = "municipalityId";

    @ParameterizedTest
    @ValueSource(strings = {"#some.identifier", "some.identifier:", "some.identifier:1", "some.identifier:x.y"})
    void testCreateWithInvalidInput(final String input) {
        assertThatExceptionOfType(IllegalArgumentException.class)
            .isThrownBy(() -> new IdentifierAndVersion(MUNICIPALITY_ID, input));
    }

    @ParameterizedTest
    @ValueSource(strings = {"some.identifier", "some.identifier:1.0"})
    void testCreateWithValidInput(final String input) {
        assertThatNoException().isThrownBy(() -> new IdentifierAndVersion(MUNICIPALITY_ID, input));
    }


    @Test
    void testEquals() {
        assertThat(new IdentifierAndVersion(MUNICIPALITY_ID, "something:1.0").equals(new Date())).isFalse();
        assertThat(new IdentifierAndVersion(MUNICIPALITY_ID, "something:1.0").equals(new IdentifierAndVersion(MUNICIPALITY_ID, "something:1.0"))).isTrue();
        assertThat(new IdentifierAndVersion(MUNICIPALITY_ID, "something:1.0").equals(new IdentifierAndVersion("other", "something:1.0"))).isFalse();
        assertThat(new IdentifierAndVersion(MUNICIPALITY_ID, "something:1.0").equals(new IdentifierAndVersion(MUNICIPALITY_ID, "something:1.1"))).isFalse();

        var v = new IdentifierAndVersion(MUNICIPALITY_ID, "something:1.0");

        assertThat(v.equals(v)).isTrue();
    }
}
