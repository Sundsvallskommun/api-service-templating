package se.sundsvall.templating.integration.db.entity;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Date;
import java.util.Optional;

import org.junit.jupiter.api.Test;

class VersionTests {

    @Test
    void testApplyVersionIncrement() {
        var version = Version.builder()
            .withMajor(3)
            .withMinor(1)
            .build();

        version = version.apply(Version.IncrementMode.MINOR);
        assertThat(version.getMajor()).isEqualTo(3);
        assertThat(version.getMinor()).isEqualTo(2);

        version = version.apply(Version.IncrementMode.MAJOR);
        assertThat(version.getMajor()).isEqualTo(4);
        assertThat(version.getMinor()).isEqualTo(0);
    }

    @Test
    void testBuilderAndGetters() {
        var version = Version.builder()
            .withMajor(2)
            .withMinor(2)
            .build();

        assertThat(version.getMajor()).isEqualTo(2);
        assertThat(version.getMinor()).isEqualTo(2);
    }

    @Test
    void testParseWithInvalidInput() {
        var version = Version.parse("a.b");

        assertThat(version).isNull();
    }

    @Test
    void testEquals() {
        assertThat(Version.parse("1.0").equals(new Date())).isFalse();
        assertThat(Version.parse("1.0").equals(Version.parse("1.0"))).isTrue();
        assertThat(Version.parse("1.0").equals(Version.parse("1.1"))).isFalse();

        var v = Version.parse("11.11");

        assertThat(v.equals(v)).isTrue();
    }

    @Test
    void testCompareTo() {
        var v1 = Optional.ofNullable(Version.parse("1.0")).orElseThrow();
        var v2 = Optional.ofNullable(Version.parse("1.1")).orElseThrow();
        var v3 = Optional.ofNullable(Version.parse("0.9")).orElseThrow();
        var v4 = Optional.ofNullable(Version.parse("1.1")).orElseThrow();

        assertThat(v1.compareTo(v2)).isEqualTo(-1);
        assertThat(v2.compareTo(v3)).isEqualTo(1);
        assertThat(v2.compareTo(v4)).isEqualTo(0);
    }
}
