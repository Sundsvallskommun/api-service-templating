package se.sundsvall.templating.api.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DirectRenderResponseTests {

	@Test
	void testBuilderAndGetters() {
		var response = DirectRenderResponse.builder()
			.withOutput("someOutput")
			.build();

		assertThat(response.getOutput()).isEqualTo("someOutput");
	}
}
