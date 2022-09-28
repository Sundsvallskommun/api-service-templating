package se.sundsvall.templating.api.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import org.junit.jupiter.api.Test;

class DirectRenderRequestTests {

    @Test
    void testGettersAndSetters() {
        var request = new DirectRenderRequest();

        request.setContent("someTemplateContents");
        request.setParameters(Map.of("someParameter", "someValue"));

        assertThat(request.getContent()).isEqualTo("someTemplateContents");
        assertThat(request.getParameters()).hasSize(1);
    }
}
