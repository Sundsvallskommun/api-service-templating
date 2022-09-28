package se.sundsvall.templating.service;

import static java.lang.String.format;
import static java.util.stream.Collectors.toMap;

import java.io.FileOutputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.TreeMap;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.JsonPatch;
import com.github.fge.jsonpatch.JsonPatchException;
import com.mitchellbosecke.pebble.PebbleEngine;
import org.apache.tomcat.util.http.fileupload.ByteArrayOutputStream;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.xhtmlrenderer.pdf.ITextRenderer;
import org.zalando.problem.Problem;
import org.zalando.problem.Status;

import se.sundsvall.templating.api.domain.DetailedTemplateResponse;
import se.sundsvall.templating.api.domain.DirectRenderRequest;
import se.sundsvall.templating.api.domain.RenderRequest;
import se.sundsvall.templating.api.domain.TemplateRequest;
import se.sundsvall.templating.api.domain.TemplateResponse;
import se.sundsvall.templating.configuration.properties.TemplateProperties;
import se.sundsvall.templating.domain.KeyValue;
import se.sundsvall.templating.exception.TemplateException;
import se.sundsvall.templating.integration.db.DbIntegration;
import se.sundsvall.templating.integration.db.entity.DefaultValueEntity;
import se.sundsvall.templating.integration.db.entity.TemplateEntity;
import se.sundsvall.templating.service.mapper.TemplatingServiceMapper;
import se.sundsvall.templating.service.pebble.DelegatingLoader;
import se.sundsvall.templating.util.BASE64;

@Service
public class TemplatingService {

    private final ObjectMapper objectMapper;
    private final PebbleEngine pebbleEngine;
    private final ITextRenderer iTextRenderer;
    private final TemplateProperties templateProperties;
    private final TemplatingServiceMapper templatingServiceMapper;
    private final DbIntegration dbIntegration;

    public TemplatingService(final ObjectMapper objectMapper, final PebbleEngine pebbleEngine,
            final ITextRenderer iTextRenderer, final TemplateProperties templateProperties,
            final TemplatingServiceMapper templatingServiceMapper, final DbIntegration dbIntegration) {
        this.objectMapper = objectMapper;
        this.pebbleEngine = pebbleEngine;
        this.iTextRenderer = iTextRenderer;
        this.templateProperties = templateProperties;
        this.templatingServiceMapper = templatingServiceMapper;
        this.dbIntegration = dbIntegration;
    }

    public String renderTemplate(final RenderRequest request) {
        return renderTemplateInternal(request, true);
    }

    String renderTemplateInternal(final RenderRequest request, final boolean base64EncodeOutput) {
        var template = Optional.ofNullable(request.getIdentifier())
            .map(dbIntegration::getTemplate)
            .orElseGet(() -> dbIntegration.findTemplate(request.getMetadata()))
            .orElseThrow(() -> {
                var message = Optional.ofNullable(request.getIdentifier())
                    .map(ignored -> format("Unable to find template using identifier: '%s'", request.getIdentifier()))
                    .orElseGet(() -> format("Unable to find template using metadata: %s", request.getMetadata()));

                return Problem.valueOf(Status.NOT_FOUND, message);
            });

        // Extract template default values
        var defaultValues = template.getDefaultValues().stream()
            .collect(toMap(DefaultValueEntity::getFieldName, DefaultValueEntity::getValue));

        // Merge default values and request parameters, default values first, allowing for request
        // parameters to override any matching default value. Also, (configurable) use a TreeMap
        // with a case-insensitive comparator, to allow the use of case-insensitive keys
        var mergedParametersAndDefaultValues = new TreeMap<>(
            templateProperties.isUseCaseInsensitiveKeys() ? String.CASE_INSENSITIVE_ORDER : null);
        mergedParametersAndDefaultValues.putAll(defaultValues);
        mergedParametersAndDefaultValues.putAll(request.getParameters());

        try (var writer = new StringWriter()) {
            pebbleEngine.getTemplate(template.getIdentifier()).evaluate(writer, mergedParametersAndDefaultValues);

            // Optionally encode the output as BASE64
            return base64EncodeOutput ? BASE64.encode(writer.toString()) : writer.toString();
        } catch (Exception e) {
            throw new TemplateException(e);
        }
    }

    public String renderTemplateAsPdf(final RenderRequest request) {
        var renderedTemplate = renderTemplateInternal(request, false);

        var os = new ByteArrayOutputStream();

        var document = Jsoup.parse(renderedTemplate, StandardCharsets.UTF_8.name());
        document.outputSettings().syntax(Document.OutputSettings.Syntax.xml);

        iTextRenderer.setDocumentFromString(document.html());
        iTextRenderer.layout();
        iTextRenderer.createPDF(os);

        try {
            iTextRenderer.createPDF(new FileOutputStream("/tmp/tmp/apa.pdf"));
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }

        iTextRenderer.finishPDF();

        // Encode the output as BASE64
        return BASE64.encode(os.toByteArray());
    }

    public String renderDirect(final DirectRenderRequest request) {
        try (var writer = new StringWriter()) {
            pebbleEngine.getTemplate(DelegatingLoader.DIRECT_PREFIX + request.getContent())
                .evaluate(writer, request.getParameters());

            var output = writer.toString().substring(DelegatingLoader.DIRECT_PREFIX.length());

            // Encode the output as BASE64
            return BASE64.encode(output);
        } catch (Exception e) {
            throw new TemplateException(e);
        }
    }

    @Transactional(readOnly = true)
    public List<TemplateResponse> getAllTemplates(final List<KeyValue> metadata) {
        if (metadata == null || metadata.isEmpty()) {
            return dbIntegration.getAllTemplates().stream()
                .map(templatingServiceMapper::toTemplateResponse)
                .toList();
        } else {
            return dbIntegration.findTemplates(metadata).stream()
                .map(templatingServiceMapper::toTemplateResponse)
                .toList();
        }
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
