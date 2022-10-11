package se.sundsvall.templating.service.mapper;

import static java.util.stream.Collectors.toSet;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import se.sundsvall.templating.api.domain.DefaultValue;
import se.sundsvall.templating.api.domain.DetailedTemplateResponse;
import se.sundsvall.templating.api.domain.Metadata;
import se.sundsvall.templating.api.domain.TemplateRequest;
import se.sundsvall.templating.api.domain.TemplateResponse;
import se.sundsvall.templating.integration.db.entity.DefaultValueEntity;
import se.sundsvall.templating.integration.db.entity.MetadataEntity;
import se.sundsvall.templating.integration.db.entity.TemplateEntity;

@Component
public class TemplateMapper {

    public TemplateEntity toTemplateEntity(final TemplateRequest templateRequest) {
        if (templateRequest == null) {
            return null;
        }

        return TemplateEntity.builder()
            .withIdentifier(templateRequest.getIdentifier())
            .withName(templateRequest.getName())
            .withDescription(templateRequest.getDescription())
            .withContent(templateRequest.getContent())
            .withChangeLog(templateRequest.getChangeLog())
            .withMetadata(Optional.ofNullable(templateRequest.getMetadata()).orElse(List.of()).stream()
                .map(metadata -> MetadataEntity.builder()
                    .withKey(metadata.getKey())
                    .withValue(metadata.getValue())
                    .build())
                .toList())
            .withDefaultValues(Optional.ofNullable(templateRequest.getDefaultValues()).orElse(List.of()).stream()
                .map(defaultValue -> DefaultValueEntity.builder()
                    .withFieldName(defaultValue.getFieldName())
                    .withValue(defaultValue.getValue())
                    .build())
                .collect(toSet()))
            .build();
    }

    public TemplateResponse toTemplateResponse(final TemplateEntity templateEntity) {
        if (templateEntity == null) {
            return null;
        }

        return TemplateResponse.builder()
            .withIdentifier(templateEntity.getIdentifier())
            .withVersion(templateEntity.getVersion().toString())
            .withName(templateEntity.getName())
            .withDescription(templateEntity.getDescription())
            .withMetadata(templateEntity.getMetadata().stream()
                .map(templateEntityMetadata -> Metadata.builder()
                    .withKey(templateEntityMetadata.getKey())
                    .withValue(templateEntityMetadata.getValue())
                    .build())
                .toList())
            .withDefaultValues(templateEntity.getDefaultValues().stream()
                .map(templateEntityDefaultValue -> DefaultValue.builder()
                    .withFieldName(templateEntityDefaultValue.getFieldName())
                    .withValue(templateEntityDefaultValue.getValue())
                    .build())
                .toList())
            .withChangeLog(templateEntity.getChangeLog())
            .withLastModifiedAt(templateEntity.getLastModifiedAt())
            .build();
    }

    public DetailedTemplateResponse toDetailedTemplateResponse(final TemplateEntity templateEntity) {
        if (templateEntity == null) {
            return null;
        }

        return DetailedTemplateResponse.builder()
            .withIdentifier(templateEntity.getIdentifier())
            .withVersion(templateEntity.getVersion().toString())
            .withName(templateEntity.getName())
            .withDescription(templateEntity.getDescription())
            .withMetadata(templateEntity.getMetadata().stream()
                .map(templateEntityMetadata -> Metadata.builder()
                    .withKey(templateEntityMetadata.getKey())
                    .withValue(templateEntityMetadata.getValue())
                    .build())
                .toList())
            .withDefaultValues(templateEntity.getDefaultValues().stream()
                .map(templateEntityDefaultValue -> DefaultValue.builder()
                    .withFieldName(templateEntityDefaultValue.getFieldName())
                    .withValue(templateEntityDefaultValue.getValue())
                    .build())
                .toList())
            .withContent(templateEntity.getContent())
            .withChangeLog(templateEntity.getChangeLog())
            .withLastModifiedAt(templateEntity.getLastModifiedAt())
            .build();
    }
}
