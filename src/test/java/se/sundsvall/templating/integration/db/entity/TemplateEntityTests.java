package se.sundsvall.templating.integration.db.entity;

import org.junit.jupiter.api.Test;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;

class TemplateEntityTests {

	@Test
	void getContentBytes() {
		var content = "someContent";
		var contentEntity = TemplateContentEntity.builder()
			.withContent(content)
			.build();

		var templateEntity = TemplateEntity.builder()
			.withTemplateContentEntity(contentEntity)
			.build();

		assertThat(templateEntity.getContentBytes()).isEqualTo(content.getBytes(UTF_8));
	}

	@Test
	void getContentBytesWhenContentIsNull() {
		assertThat(new TemplateEntity().getContentBytes()).isNull();
	}

	@Test
	void latestDefaultsToFalse() {
		var entity = new TemplateEntity();
		assertThat(entity.isLatest()).isFalse();
	}

	@Test
	void latestDefaultsToFalseWhenUsingBuilder() {
		var entity = TemplateEntity.builder().build();
		assertThat(entity.isLatest()).isFalse();
	}

	@Test
	void latestCanBeSetViaBuilder() {
		var entity = TemplateEntity.builder().withLatest(true).build();
		assertThat(entity.isLatest()).isTrue();
	}

	@Test
	void latestCanBeSetViaSetter() {
		var entity = new TemplateEntity();
		entity.setLatest(true);
		assertThat(entity.isLatest()).isTrue();
	}
}
