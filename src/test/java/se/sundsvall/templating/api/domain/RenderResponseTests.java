package se.sundsvall.templating.api.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class RenderResponseTests {

    @Test
    void testBuilderAndGetters() {
        var response = RenderResponse.builder()
            .withOutput("someContent")
            .build();

        assertThat(response.getOutput()).isEqualTo("someContent");
    }
}
