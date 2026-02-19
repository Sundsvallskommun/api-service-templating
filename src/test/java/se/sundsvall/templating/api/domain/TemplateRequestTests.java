package se.sundsvall.templating.api.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TemplateRequestTests {

	@Test
	void testGettersAndSetters() {
		var request = new TemplateRequest();
		request.setName("someName");
		request.setDescription("someDescription");
		request.setContent("someContent");

		assertThat(request.getName()).isEqualTo("someName");
		assertThat(request.getDescription()).isEqualTo("someDescription");
		assertThat(request.getContent()).isEqualTo("someContent");
	}
}
