package se.sundsvall.templating.service;

import java.io.IOException;
import java.io.StringWriter;
import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.JsonPatch;
import com.github.fge.jsonpatch.JsonPatchException;
import com.mitchellbosecke.pebble.PebbleEngine;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.zalando.problem.Problem;
import org.zalando.problem.Status;

import se.sundsvall.templating.api.domain.DetailedTemplateResponse;
import se.sundsvall.templating.api.domain.DirectRenderRequest;
import se.sundsvall.templating.api.domain.RenderRequest;
import se.sundsvall.templating.api.domain.TemplateRequest;
import se.sundsvall.templating.api.domain.TemplateResponse;
import se.sundsvall.templating.integration.db.DbIntegration;
import se.sundsvall.templating.integration.db.entity.TemplateEntity;
import se.sundsvall.templating.service.mapper.TemplatingServiceMapper;
import se.sundsvall.templating.service.pebble.DelegatingLoader;

@Service
public class TemplatingService {

    private final DbIntegration dbIntegration;
    private final PebbleEngine pebbleEngine;
    private final ObjectMapper objectMapper;
    private final TemplatingServiceMapper templatingServiceMapper;

    public TemplatingService(final DbIntegration dbIntegration, final PebbleEngine pebbleEngine,
            final ObjectMapper objectMapper,
            final TemplatingServiceMapper templatingServiceMapper) {
        this.dbIntegration = dbIntegration;
        this.pebbleEngine = pebbleEngine;
        this.objectMapper = objectMapper;
        this.templatingServiceMapper = templatingServiceMapper;
    }

    public String renderTemplate(final RenderRequest request) {
        return dbIntegration.getTemplate(request.getTemplateIdentifier())
            .map(template -> {
                try (var writer = new StringWriter()) {
                    pebbleEngine.getTemplate(template.getId()).evaluate(writer, request.getParameters());

                    return writer.toString();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            })
            .orElseThrow(() -> Problem.valueOf(Status.NOT_FOUND, "Unable to find template with identifier '" + request.getTemplateIdentifier() + "'"));
    }

    public String renderDirect(final DirectRenderRequest request) {
        try (var writer = new StringWriter()) {
            pebbleEngine.getTemplate(DelegatingLoader.DIRECT_PREFIX + request.getTemplate())
                .evaluate(writer, request.getParameters());

            return writer.toString().substring(DelegatingLoader.DIRECT_PREFIX.length());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Transactional(readOnly = true)
    public List<TemplateResponse> getAllTemplates() {
        return dbIntegration.getAllTemplates().stream()
            .map(templatingServiceMapper::toTemplateResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public Optional<DetailedTemplateResponse> getTemplate(final String identifier) {
        return dbIntegration.getTemplate(identifier)
            .map(templatingServiceMapper::toDetailedTemplateResponse);
    }

    public TemplateResponse saveTemplate(final TemplateRequest templateRequest) {
        return templatingServiceMapper.toTemplateResponse(
            dbIntegration.saveTemplate(
                templatingServiceMapper.toTemplateEntity(templateRequest)));
    }

    public TemplateResponse updateTemplate(final String identifier, final JsonPatch jsonPatch) {
        return dbIntegration.getTemplate(identifier)
            .map(templateEntity -> applyPatch(jsonPatch, TemplateEntity.class, templateEntity))
            .map(dbIntegration::saveTemplate)
            .map(templatingServiceMapper::toTemplateResponse)
            .orElseThrow(() -> Problem.valueOf(Status.NOT_FOUND, "Unable to find template with identifier '" + identifier + "'"));
    }

    public void deleteTemplate(final String identifier) {
        dbIntegration.deleteTemplate(identifier);
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
