package se.sundsvall.templating.api.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RenderResponseTests {

	@Test
	void testBuilderAndGetters() {
		var response = RenderResponse.builder()
			.withOutput("someContent")
			.build();

		assertThat(response.getOutput()).isEqualTo("someContent");
	}
}
