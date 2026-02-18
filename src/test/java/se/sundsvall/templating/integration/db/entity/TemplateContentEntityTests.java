package se.sundsvall.templating.integration.db.entity;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TemplateContentEntityTests {

	@Test
	void builder() {
		final var template = TemplateEntity.builder().build();
		final var content = TemplateContentEntity.builder()
			.withTemplate(template)
			.withContent("someContent")
			.build();

		assertThat(content.getTemplate()).isEqualTo(template);
		assertThat(content.getContent()).isEqualTo("someContent");
	}
}
