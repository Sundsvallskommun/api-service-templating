package se.sundsvall.templating.api.domain.filter.expression;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EmptyTests {

	@Test
	void testToString() {
		assertThat(new Empty().toString()).isEqualTo("<EMPTY>");
	}
}
