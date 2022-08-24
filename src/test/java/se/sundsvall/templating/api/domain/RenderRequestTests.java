package se.sundsvall.templating.api.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import se.sundsvall.templating.domain.KeyValue;

class RenderRequestTests {

    @Test
    void testBuilderAndGetters() {
        var request = RenderRequest.builder()
            .withIdentifier("someTemplateId")
            .withMetadata(List.of(KeyValue.of("someKey", "someValue")))
            .withParameters(Map.of("someParameterName", "someParameterValue"))
            .build();

        assertThat(request.getIdentifier()).isEqualTo("someTemplateId");
        assertThat(request.getMetadata()).hasSize(1);
        assertThat(request.getParameters()).hasSize(1);
    }
}
