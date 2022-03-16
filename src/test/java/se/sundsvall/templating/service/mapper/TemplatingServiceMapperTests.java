package se.sundsvall.templating.service.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import org.junit.jupiter.api.Test;

import se.sundsvall.templating.TemplateFlavor;
import se.sundsvall.templating.api.domain.TemplateRequest;
import se.sundsvall.templating.integration.db.entity.TemplateEntity;

class TemplatingServiceMapperTests {

    private final TemplatingServiceMapper mapper = new TemplatingServiceMapper();

    @Test
    void test_toTemplateEntity() {
        var request = new TemplateRequest();
        request.setName("someName");
        request.setDescription("someDescription");
        request.setVariants(Map.of(TemplateFlavor.TEXT, "someContent"));

        var templateEntity = mapper.toTemplateEntity(request);

        assertThat(templateEntity).isNotNull();
        assertThat(templateEntity.getName()).isEqualTo("someName");
        assertThat(templateEntity.getDescription()).isEqualTo("someDescription");
        assertThat(templateEntity.getVariants().keySet()).containsExactly(TemplateFlavor.TEXT);
        assertThat(templateEntity.getVariants().values()).containsExactly("someContent");
    }

    @Test
    void test_toTemplateEntity_whenTemplateRequestIsNull() {
        assertThat(mapper.toTemplateEntity(null)).isNull();
    }

    @Test
    void test_toTemplateResponse() {
        var templateEntity = TemplateEntity.builder()
            .withId("someId")
            .withName("someName")
            .withDescription("someDescription")
            .withVariants(Map.of(TemplateFlavor.TEXT, "someContent"))
            .build();

        var templateResponse = mapper.toTemplateResponse(templateEntity);

        assertThat(templateResponse).isNotNull();
        assertThat(templateResponse.getId()).isEqualTo(templateEntity.getId());
        assertThat(templateResponse.getName()).isEqualTo(templateEntity.getName());
        assertThat(templateResponse.getDescription()).isEqualTo(templateEntity.getDescription());
        assertThat(templateResponse.getVariants().keySet()).containsExactly(TemplateFlavor.TEXT);
        assertThat(templateResponse.getVariants().values()).containsExactly("someContent");
    }

    @Test
    void test_toTemplateResponse_whenTemplateEntityIsNull() {
        assertThat(mapper.toTemplateResponse(null)).isNull();
    }
}
