package se.sundsvall.templating.api.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import org.junit.jupiter.api.Test;

class RenderRequestTests {

    @Test
    void testGettersAndSetters() {
        var request = new RenderRequest();

        request.setTemplateIdentifier("someTemplateId");
        request.setParameters(Map.of("someParameter", "someValue"));

        assertThat(request.getTemplateIdentifier()).isEqualTo("someTemplateId");
        assertThat(request.getParameters()).hasSize(1);
    }
}
