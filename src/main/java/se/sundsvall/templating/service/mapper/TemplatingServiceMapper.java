package se.sundsvall.templating.service.mapper;

import org.springframework.stereotype.Component;

import se.sundsvall.templating.api.domain.DetailedTemplateResponse;
import se.sundsvall.templating.api.domain.TemplateRequest;
import se.sundsvall.templating.api.domain.TemplateResponse;
import se.sundsvall.templating.integration.db.entity.TemplateEntity;

@Component
public class TemplatingServiceMapper {

    public TemplateEntity toTemplateEntity(final TemplateRequest templateRequest) {
        if (templateRequest == null) {
            return null;
        }

        return TemplateEntity.builder()
            .withName(templateRequest.getName())
            .withDescription(templateRequest.getDescription())
            .withContent(templateRequest.getContent())
            .build();
    }

    public TemplateResponse toTemplateResponse(final TemplateEntity templateEntity) {
        if (templateEntity == null) {
            return null;
        }

        return TemplateResponse.builder()
            .withIdentifier(templateEntity.getIdentifier())
            .withName(templateEntity.getName())
            .withDescription(templateEntity.getDescription())
            .build();
    }

    public DetailedTemplateResponse toDetailedTemplateResponse(final TemplateEntity templateEntity) {
        if (templateEntity == null) {
            return null;
        }

        return DetailedTemplateResponse.builder()
            .withIdentifier(templateEntity.getId())
            .withName(templateEntity.getName())
            .withDescription(templateEntity.getDescription())
            .withContent(templateEntity.getContent())
            .build();
    }
}
