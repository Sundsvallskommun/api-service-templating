package se.sundsvall.templating.api.domain.filter.expression.logic;

import org.junit.jupiter.api.Test;
import se.sundsvall.templating.api.domain.filter.expression.value.Eq;

import static org.assertj.core.api.Assertions.assertThat;

class NotTests {

	@Test
	void constructorAndGetters() {
		var eq = new Eq("someKey", "someValue");
		var not = new Not(eq);

		assertThat(not.expression()).isEqualTo(eq);
	}

	@Test
	void testToString() {
		var eq = new Eq("someKey", "someValue");
		var not = new Not(eq);

		assertThat(not.toString()).isEqualTo(String.format("NOT %s", eq));
	}
}
