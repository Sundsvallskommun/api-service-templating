package se.sundsvall.templating.api.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class DirectRenderResponseTests {

    @Test
    void testBuilderAndGetters() {
        var response = DirectRenderResponse.builder()
            .withOutput("someOutput")
            .build();

        assertThat(response.getOutput()).isEqualTo("someOutput");
    }
}
