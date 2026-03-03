package se.sundsvall.templating.api.domain.filter.expression.logic;

import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import se.sundsvall.templating.api.domain.filter.expression.Expression;
import se.sundsvall.templating.api.domain.filter.expression.value.Eq;

import static java.util.stream.Collectors.joining;
import static org.assertj.core.api.Assertions.assertThat;

class AndTests {

	@Test
	void constructorAndGetters() {
		var eq1 = new Eq("someKey", "someValue");
		var eq2 = new Eq("someOtherKey", "someOtherValue");
		var and = new And(List.of(eq1, eq2));

		assertThat(and.expressions).containsExactlyInAnyOrder(eq1, eq2);
	}

	@Test
	void testToString() {
		var eq1 = new Eq("someKey", "someValue");
		var eq2 = new Eq("someOtherKey", "someOtherValue");
		var and = new And(List.of(eq1, eq2));

		assertThat(and.toString()).hasToString(
			Stream.of(eq1, eq2).map(Expression::toString).collect(joining(" AND ", "(", ")")));
	}
}
