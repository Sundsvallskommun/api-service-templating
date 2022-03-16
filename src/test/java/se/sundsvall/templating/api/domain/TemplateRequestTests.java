package se.sundsvall.templating.api.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import org.junit.jupiter.api.Test;

import se.sundsvall.templating.TemplateFlavor;

class TemplateRequestTests {

    @Test
    void testGettersAndSetters() {
        var request = new TemplateRequest();
        request.setName("someName");
        request.setDescription("someDescription");
        request.setVariants(Map.of(TemplateFlavor.TEXT, "someTextTemplateContent"));

        assertThat(request.getName()).isEqualTo("someName");
        assertThat(request.getDescription()).isEqualTo("someDescription");
        assertThat(request.getVariants()).hasSize(1);
    }
}
