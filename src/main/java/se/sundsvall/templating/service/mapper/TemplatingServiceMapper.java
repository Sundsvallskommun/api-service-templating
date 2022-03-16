package se.sundsvall.templating.service.mapper;

import org.springframework.stereotype.Component;

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
            .withVariants(templateRequest.getVariants())
            .build();
    }

    public TemplateResponse toTemplateResponse(final TemplateEntity templateEntity) {
        if (templateEntity == null) {
            return null;
        }

        return TemplateResponse.builder()
            .withId(templateEntity.getId())
            .withName(templateEntity.getName())
            .withDescription(templateEntity.getDescription())
            .withVariants(templateEntity.getVariants())
            .build();
    }
}
