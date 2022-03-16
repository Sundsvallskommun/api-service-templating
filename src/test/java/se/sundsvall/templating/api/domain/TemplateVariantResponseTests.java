package se.sundsvall.templating.api.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class TemplateVariantResponseTests {

    @Test
    void testBuilderAndGetters() {
        var response = TemplateVariantResponse.builder()
            .withContent("someContent")
            .build();

        assertThat(response.getContent()).isEqualTo("someContent");
    }
}
