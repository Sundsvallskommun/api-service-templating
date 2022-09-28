package se.sundsvall.templating.api.domain.validation;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

import se.sundsvall.templating.api.domain.RenderRequest;
import se.sundsvall.templating.domain.KeyValue;

class ValidRenderRequestValidatorTests {

    private final ValidRenderRequestValidator validator = new ValidRenderRequestValidator();

    @Test
    void test_nullValues_isNotValid() {
        var request = RenderRequest.builder().build();

        assertThat(validator.isValid(request, null)).isFalse();
    }

    @Test
    void test_emptyValues_isNotValid() {
        var request = RenderRequest.builder()
            .withIdentifier("")
            .withMetadata(List.of())
            .build();

        assertThat(validator.isValid(request, null)).isFalse();
    }

    @Test
    void test_templateIdSetAndMetadataEmpty_isValid() {
        var request = RenderRequest.builder()
            .withIdentifier("someTemplateId")
            .withMetadata(List.of())
            .build();

        assertThat(validator.isValid(request, null)).isTrue();
    }

    @Test
    void test_templateIdEmptyAndMetadataNonEmpty_isValid() {
        var request = RenderRequest.builder()
            .withIdentifier("")
            .withMetadata(List.of(KeyValue.of("someKey", "someValue")))
            .build();

        assertThat(validator.isValid(request, null)).isTrue();
    }

    @Test
    void test_bothValues_isNotValid() {
        var request = RenderRequest.builder()
            .withIdentifier("someTemplateId")
            .withMetadata(List.of(KeyValue.of("someKey", "someValue")))
            .build();

        assertThat(validator.isValid(request, null)).isFalse();
    }

}
