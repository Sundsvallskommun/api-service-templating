package se.sundsvall.templating.api.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class DetailedTemplateResponseTests {

    @Test
    void testBuilderAndGetters() {
        var response = DetailedTemplateResponse.builder()
            .withIdentifier("someId")
            .withName("someName")
            .withDescription("someDescription")
            .withContent("someContent")
            .build();

        assertThat(response.getIdentifier()).isEqualTo("someId");
        assertThat(response.getName()).isEqualTo("someName");
        assertThat(response.getDescription()).isEqualTo("someDescription");
        assertThat(response.getContent()).isEqualTo("someContent");
    }
}
