package se.sundsvall.templating.integration.db.entity;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class TemplateEntityTests {

    @Test
    void getContentBytes() {
        var content = "someContent";

        var templateEntity = TemplateEntity.builder()
            .withContent(content)
            .build();

        assertThat(templateEntity.getContentBytes()).isEqualTo(content.getBytes(UTF_8));
    }

    @Test
    void getContentBytesWhenContentIsNull() {
        assertThat(new TemplateEntity().getContentBytes()).isNull();
    }
}
