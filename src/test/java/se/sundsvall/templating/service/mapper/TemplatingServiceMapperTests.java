package se.sundsvall.templating.service.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import se.sundsvall.templating.api.domain.TemplateRequest;
import se.sundsvall.templating.integration.db.entity.TemplateEntity;

class TemplatingServiceMapperTests {

    private final TemplatingServiceMapper mapper = new TemplatingServiceMapper();

    @Test
    void test_toTemplateEntity() {
        var request = new TemplateRequest();
        request.setName("someName");
        request.setDescription("someDescription");
        request.setContent("someContent");

        var templateEntity = mapper.toTemplateEntity(request);

        assertThat(templateEntity).isNotNull();
        assertThat(templateEntity.getName()).isEqualTo("someName");
        assertThat(templateEntity.getDescription()).isEqualTo("someDescription");
        assertThat(templateEntity.getContent()).isEqualTo("someContent");
    }

    @Test
    void test_toTemplateEntity_whenTemplateRequestIsNull() {
        assertThat(mapper.toTemplateEntity(null)).isNull();
    }

    @Test
    void test_toTemplateResponse() {
        var templateEntity = TemplateEntity.builder()
            .withIdentifier("someIdentifier")
            .withName("someName")
            .withDescription("someDescription")
            .withContent("someContent")
            .build();

        var templateResponse = mapper.toTemplateResponse(templateEntity);

        assertThat(templateResponse).isNotNull();
        assertThat(templateResponse.getIdentifier()).isNotBlank();
        assertThat(templateResponse.getName()).isEqualTo(templateEntity.getName());
        assertThat(templateResponse.getDescription()).isEqualTo(templateEntity.getDescription());
    }

    @Test
    void test_toTemplateResponse_whenTemplateEntityIsNull() {
        assertThat(mapper.toTemplateResponse(null)).isNull();
    }

    @Test
    void test_toDetailedTemplateResponse() {
        var templateEntity = TemplateEntity.builder()
            .withName("someName")
            .withDescription("someDescription")
            .withContent("someContent")
            .build();

        var detailedTemplateResponse = mapper.toDetailedTemplateResponse(templateEntity);

        assertThat(detailedTemplateResponse).isNotNull();
        assertThat(detailedTemplateResponse.getIdentifier()).isNotBlank();
        assertThat(detailedTemplateResponse.getName()).isEqualTo(templateEntity.getName());
        assertThat(detailedTemplateResponse.getDescription()).isEqualTo(templateEntity.getDescription());
        assertThat(detailedTemplateResponse.getContent()).isEqualTo("someContent");
    }

    @Test
    void test_toDetailedTemplateResponse_whenTemplateEntityIsNull() {
        assertThat(mapper.toDetailedTemplateResponse(null)).isNull();
    }
}
