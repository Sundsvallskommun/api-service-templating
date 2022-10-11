package se.sundsvall.templating.service;

import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.JsonPatch;
import com.github.fge.jsonpatch.JsonPatchException;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.zalando.problem.Problem;
import org.zalando.problem.Status;

import se.sundsvall.templating.api.domain.DetailedTemplateResponse;
import se.sundsvall.templating.api.domain.TemplateRequest;
import se.sundsvall.templating.api.domain.TemplateResponse;
import se.sundsvall.templating.domain.KeyValue;
import se.sundsvall.templating.integration.db.DbIntegration;
import se.sundsvall.templating.integration.db.entity.TemplateEntity;
import se.sundsvall.templating.integration.db.entity.Version;
import se.sundsvall.templating.service.mapper.TemplateMapper;

@Service
public class TemplateService {

    private final ObjectMapper objectMapper;
    private final DbIntegration dbIntegration;
    private final TemplateMapper mapper;

    public TemplateService(final ObjectMapper objectMapper,
            final DbIntegration dbIntegration, final TemplateMapper mapper) {
        this.objectMapper = objectMapper;
        this.mapper = mapper;
        this.dbIntegration = dbIntegration;
    }

    @Transactional(readOnly = true)
    public List<TemplateResponse> getTemplates(final Specification<TemplateEntity> filter) {
        return dbIntegration.findTemplates(filter).stream().map(mapper::toTemplateResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<TemplateResponse> getTemplates(final List<KeyValue> metadata) {
        if (metadata == null || metadata.isEmpty()) {
            return dbIntegration.getAllTemplates().stream()
                .map(mapper::toTemplateResponse)
                .toList();
        } else {
            return dbIntegration.findTemplates(metadata).stream()
                .map(mapper::toTemplateResponse)
                .toList();
        }
    }

    @Transactional(readOnly = true)
    public Optional<DetailedTemplateResponse> getTemplate(final String identifier, final String version) {
        return dbIntegration.getTemplate(identifier, version)
            .map(mapper::toDetailedTemplateResponse);
    }

    public TemplateResponse saveTemplate(final TemplateRequest templateRequest) {
        var version = dbIntegration.getTemplate(templateRequest.getIdentifier(), null)
            .map(templateEntity -> {
                if (null == templateRequest.getVersionIncrement()) {
                    throw Problem.valueOf(Status.BAD_REQUEST, "'versionIncrement' must be set, since template with identifier '" + templateRequest.getIdentifier() + "' already exists");
                }

                return templateEntity.getVersion().apply(templateRequest.getVersionIncrement());
            })
            .orElse(Version.INITIAL_VERSION);

        var templateEntity = mapper.toTemplateEntity(templateRequest)
            .withVersion(version);

        return mapper.toTemplateResponse(dbIntegration.saveTemplate(templateEntity));
    }

    public TemplateResponse updateTemplate(final String identifier, final String version, final JsonPatch jsonPatch) {
        return dbIntegration.getTemplate(identifier, version)
            .map(templateEntity -> applyPatch(jsonPatch, TemplateEntity.class, templateEntity))
            .map(dbIntegration::saveTemplate)
            .map(mapper::toTemplateResponse)
            .orElseThrow(() -> Problem.valueOf(Status.NOT_FOUND, "Unable to find template '" + identifier + ":" + version + "'"));
    }

    public void deleteTemplate(final String identifier, final String version) {
        dbIntegration.deleteTemplate(identifier, version);
    }

    <T> T applyPatch(final JsonPatch patch, final Class<T> targetClass, final T target) {
        try {
            var patched = patch.apply(objectMapper.convertValue(target, JsonNode.class));

            return objectMapper.treeToValue(patched, targetClass);
        } catch (JsonPatchException | JsonProcessingException e) {
            throw new IllegalStateException("Unable to patch template entity", e);
        }
    }
}
