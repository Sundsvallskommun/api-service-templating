package se.sundsvall.templating.api.domain.filter.expression.value;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ValueExpressionTests {

	static class Dummy extends ValueExpression<String> {

		Dummy(final String key, final String value) {
			super(key, value);
		}
	}

	@Test
	void testHashCode() {
		var key = "someKey";
		var value = "someValue";

		var dummy1 = new Dummy(key, value);
		var dummy2 = new Dummy(key, value);

		assertThat(dummy1.hashCode()).isEqualTo(dummy2.hashCode());
	}

	@Test
	void testEquals() {
		var key = "someKey";
		var value = "someValue";

		var dummy1 = new Dummy(key, value);
		var dummy2 = new Dummy(key, value);
		var dummy3 = new Dummy(key, "someOtherValue");
		var dummy4 = new Dummy("someOtherKey", value);

		assertThat(dummy1).isEqualTo(dummy2);
		assertThat(dummy1).isNotEqualTo(dummy3);
		assertThat(dummy1).isNotEqualTo(dummy4);
		assertThat(dummy1).isNotEqualTo(null);
	}
}
