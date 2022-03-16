package se.sundsvall.templating.api.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import org.junit.jupiter.api.Test;

import se.sundsvall.templating.TemplateFlavor;

class RenderResponseTests {

    @Test
    void testBuilderAndGetters() {
        var response = RenderResponse.builder()
            .withOutput(Map.of(TemplateFlavor.TEXT, "someContent"))
            .build();

        assertThat(response.getOutput()).hasSize(1);
    }
}
