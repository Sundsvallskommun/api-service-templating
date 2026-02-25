package se.sundsvall.templating.api.domain.filter.expression.value;

import java.util.List;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class InTests {

	@Test
	void constructorAndGetters() {
		var key = "someKey";
		var values = List.of("someValue", "someOtherValue");

		var in = new In(key, values);

		assertThat(in.getKey()).isEqualTo(key);
		assertThat(in.getValue()).isEqualTo(values);
	}

	@Test
	void testToString() {
		var key = "someKey";
		var values = List.of("someValue", "someOtherValue");

		var in = new In(key, values);

		assertThat(in.toString()).isEqualTo(String.format("%s IN %s", key, values));
	}
}
