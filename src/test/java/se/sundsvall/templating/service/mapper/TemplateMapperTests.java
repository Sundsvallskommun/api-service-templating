package se.sundsvall.templating.service.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import se.sundsvall.templating.api.domain.DefaultValue;
import se.sundsvall.templating.api.domain.Metadata;
import se.sundsvall.templating.api.domain.TemplateRequest;
import se.sundsvall.templating.integration.db.entity.DefaultValueEntity;
import se.sundsvall.templating.integration.db.entity.MetadataEntity;
import se.sundsvall.templating.integration.db.entity.TemplateEntity;
import se.sundsvall.templating.util.BASE64;

class TemplateMapperTests {

    private final TemplateMapper mapper = new TemplateMapper();

    @Test
    void test_toTemplateEntity() {
        var request = new TemplateRequest();
        request.setName("someName");
        request.setDescription("someDescription");
        request.setContent("someContent");
        request.setMetadata(List.of(Metadata.builder().build()));
        request.setDefaultValues(List.of(DefaultValue.builder().build()));

        var templateEntity = mapper.toTemplateEntity(request);

        assertThat(templateEntity).isNotNull();
        assertThat(templateEntity.getName()).isEqualTo("someName");
        assertThat(templateEntity.getDescription()).isEqualTo("someDescription");
        assertThat(templateEntity.getContent()).isEqualTo("someContent");
        assertThat(templateEntity.getMetadata()).hasSameSizeAs(request.getMetadata());
        assertThat(templateEntity.getDefaultValues()).hasSameSizeAs(request.getDefaultValues());
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
            .withMetadata(List.of(MetadataEntity.builder().build()))
            .withDefaultValues(Set.of(DefaultValueEntity.builder().build()))
            .withContent("someContent")
            .build();

        var templateResponse = mapper.toTemplateResponse(templateEntity);

        assertThat(templateResponse).isNotNull();
        assertThat(templateResponse.getIdentifier()).isNotBlank();
        assertThat(templateResponse.getName()).isEqualTo(templateEntity.getName());
        assertThat(templateResponse.getDescription()).isEqualTo(templateEntity.getDescription());
        assertThat(templateResponse.getDescription()).isEqualTo(templateEntity.getDescription());
        assertThat(templateResponse.getMetadata()).hasSameSizeAs(templateEntity.getMetadata());
        assertThat(templateResponse.getDefaultValues()).hasSameSizeAs(templateEntity.getDefaultValues());
    }

    @Test
    void test_toTemplateResponse_whenTemplateEntityIsNull() {
        assertThat(mapper.toTemplateResponse(null)).isNull();
    }

    @Test
    void test_toDetailedTemplateResponse() {
        var templateEntity = TemplateEntity.builder()
            .withIdentifier("someIdentifier")
            .withName("someName")
            .withDescription("someDescription")
            .withMetadata(List.of(MetadataEntity.builder().build()))
            .withDefaultValues(Set.of(DefaultValueEntity.builder().build()))
            .withContent(BASE64.encode("someContent"))
            .build();

        var detailedTemplateResponse = mapper.toDetailedTemplateResponse(templateEntity);

        assertThat(detailedTemplateResponse).isNotNull();
        assertThat(detailedTemplateResponse.getIdentifier()).isNotBlank();
        assertThat(detailedTemplateResponse.getName()).isEqualTo(templateEntity.getName());
        assertThat(detailedTemplateResponse.getDescription()).isEqualTo(templateEntity.getDescription());
        assertThat(detailedTemplateResponse.getContent()).isEqualTo(BASE64.encode("someContent"));
        assertThat(detailedTemplateResponse.getMetadata()).hasSameSizeAs(templateEntity.getMetadata());
        assertThat(detailedTemplateResponse.getDefaultValues()).hasSameSizeAs(templateEntity.getDefaultValues());
    }

    @Test
    void test_toDetailedTemplateResponse_whenTemplateEntityIsNull() {
        assertThat(mapper.toDetailedTemplateResponse(null)).isNull();
    }
}
