package se.sundsvall.templating.api.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

import se.sundsvall.templating.TemplateFlavor;

class TemplatesResponseTests {

    @Test
    void testBuilderAndGetters() {
        var response = TemplatesResponse.builder()
            .withId("someId")
            .withName("someName")
            .withDescription("someDescription")
            .withVariants(List.of(TemplateFlavor.TEXT, TemplateFlavor.HTML))
            .build();

        assertThat(response.getId()).isEqualTo("someId");
        assertThat(response.getName()).isEqualTo("someName");
        assertThat(response.getDescription()).isEqualTo("someDescription");
        assertThat(response.getVariants()).hasSize(2);
    }
}
