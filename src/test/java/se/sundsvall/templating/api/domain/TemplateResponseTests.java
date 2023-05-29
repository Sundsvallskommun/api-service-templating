package se.sundsvall.templating.api.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class TemplateResponseTests {

    @Test
    void testBuilderAndGetters() {
        var response = TemplateResponse.builder()
            .withIdentifier("someId")
            .withName("someName")
            .withDescription("someDescription")
            .build();

        assertThat(response.getIdentifier()).isEqualTo("someId");
        assertThat(response.getName()).isEqualTo("someName");
        assertThat(response.getDescription()).isEqualTo("someDescription");
    }
}
