package se.sundsvall.templating.api.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import org.junit.jupiter.api.Test;

import se.sundsvall.templating.TemplateFlavor;

class TemplateResponseTests {

    @Test
    void testBuilderAndGetters() {
        var response = TemplateResponse.builder()
            .withId("someId")
            .withName("someName")
            .withDescription("someDescription")
            .withVariants(Map.of(TemplateFlavor.TEXT, "someContent"))
            .build();

        assertThat(response.getId()).isEqualTo("someId");
        assertThat(response.getName()).isEqualTo("someName");
        assertThat(response.getDescription()).isEqualTo("someDescription");
        assertThat(response.getVariants()).hasSize(1);
    }
}
