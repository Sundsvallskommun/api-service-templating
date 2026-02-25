package se.sundsvall.templating.api.domain.filter.expression.value;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EqTests {

	@Test
	void constructorAndGetters() {
		var key = "someKey";
		var value = "someValue";

		var eq = new Eq(key, value);

		assertThat(eq.getKey()).isEqualTo(key);
		assertThat(eq.getValue()).isEqualTo(value);
	}

	@Test
	void testToString() {
		var key = "someKey";
		var value = "someValue";

		var eq = new Eq(key, value);

		assertThat(eq.toString()).isEqualTo(String.format("%s == %s", key, value));
	}
}
