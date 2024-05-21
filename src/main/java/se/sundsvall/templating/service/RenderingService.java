package se.sundsvall.templating.service;

import static java.lang.String.format;
import static java.util.stream.Collectors.toMap;
import static se.sundsvall.templating.util.TemplateUtil.bytesToString;
import static se.sundsvall.templating.util.TemplateUtil.decodeBase64;
import static se.sundsvall.templating.util.TemplateUtil.encodeBase64;
import static se.sundsvall.templating.util.TemplateUtil.getTemplateType;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.tomcat.util.http.fileupload.ByteArrayOutputStream;
import org.springframework.stereotype.Service;
import org.xhtmlrenderer.pdf.ITextRenderer;
import org.zalando.problem.Problem;
import org.zalando.problem.Status;

import se.sundsvall.templating.api.domain.DirectRenderRequest;
import se.sundsvall.templating.api.domain.RenderRequest;
import se.sundsvall.templating.configuration.properties.PebbleProperties;
import se.sundsvall.templating.exception.TemplateException;
import se.sundsvall.templating.integration.db.DbIntegration;
import se.sundsvall.templating.integration.db.entity.DefaultValueEntity;
import se.sundsvall.templating.service.pebble.loader.DelegatingLoader;
import se.sundsvall.templating.service.processor.PebbleTemplateProcessor;
import se.sundsvall.templating.service.processor.WordTemplateProcessor;

import fr.opensagres.poi.xwpf.converter.pdf.PdfConverter;
import fr.opensagres.poi.xwpf.converter.pdf.PdfOptions;

@Service
public class RenderingService {

    private final PebbleProperties pebbleProperties;
    private final PebbleTemplateProcessor pebbleTemplateProcessor;
    private final WordTemplateProcessor wordTemplateProcessor;
    private final ITextRenderer iTextRenderer;
    private final DbIntegration dbIntegration;

    public RenderingService(final PebbleProperties pebbleProperties,
            final PebbleTemplateProcessor pebbleTemplateProcessor,
            final WordTemplateProcessor wordTemplateProcessor,
            final ITextRenderer iTextRenderer,
            final DbIntegration dbIntegration) {
        this.pebbleProperties = pebbleProperties;
        this.pebbleTemplateProcessor = pebbleTemplateProcessor;
        this.wordTemplateProcessor = wordTemplateProcessor;
        this.iTextRenderer = iTextRenderer;
        this.dbIntegration = dbIntegration;
    }

    public String renderTemplate(final RenderRequest request) {
        var output = renderTemplateInternal(request);

        return encodeBase64(output);
    }

    public String renderTemplateAsPdf(final RenderRequest request) {
        var output = renderTemplateInternal(request);
        var renderedPdf = renderHtmlAsPdf(output);

        return encodeBase64(renderedPdf);
    }

    public String renderDirect(final DirectRenderRequest request) {
        var output = renderDirectInternal(request);

        return encodeBase64(output);
    }

    public String renderDirectAsPdf(final DirectRenderRequest request) {
        var output = renderDirectInternal(request);

        var renderedPdf = switch (getTemplateType(decodeBase64(request.getContent()))) {
            case PEBBLE -> renderHtmlAsPdf(output);
            case WORD -> renderWordAsPdf(output);
        };

        return encodeBase64(renderedPdf);
    }

    byte[] renderTemplateInternal(final RenderRequest request) {
        var template = Optional.ofNullable(request.getIdentifier())
            .flatMap(identifier -> dbIntegration.getTemplate(identifier, request.getVersion()))
            .orElseGet(() -> Optional.ofNullable(request.getMetadata()).flatMap(dbIntegration::findTemplate).orElse(null));

        if (null == template) {
            var message = Optional.ofNullable(request.getIdentifier())
                .map(ignored -> format("Unable to find template using identifier: '%s'", request.getIdentifier()))
                .orElseGet(() -> format("Unable to find template using metadata: %s", request.getMetadata()));

            throw Problem.valueOf(Status.NOT_FOUND, message);
        }

        // Extract template default values
        var defaultValues = template.getDefaultValues().stream()
            .collect(toMap(DefaultValueEntity::getFieldName, DefaultValueEntity::getValue));

        // Merge default values and request parameters, default values first, allowing for request
        // parameters to override any matching default value. Also, (configurable) use a TreeMap
        // with a case-insensitive comparator, to allow the use of case-insensitive keys
        var mergedParametersAndDefaultValues = new TreeMap<>(
            pebbleProperties.isUseCaseInsensitiveKeys() ? String.CASE_INSENSITIVE_ORDER : null);

        // Add identifier and version as extra template parameters, prefixed with underscore to
        // minimize the risk of name clashes
        mergedParametersAndDefaultValues.put("_identifier", template.getIdentifier());
        mergedParametersAndDefaultValues.put("_version", template.getVersion());
        // Add default values
        mergedParametersAndDefaultValues.putAll(defaultValues);
        // Add provided parameters
        mergedParametersAndDefaultValues.putAll(Optional.ofNullable(request.getParameters()).orElse(Map.of()));

        // Process the template
        return switch (template.getType()) {
            case PEBBLE -> pebbleTemplateProcessor.process(template.getIdentifier() + ":" + template.getVersion(), mergedParametersAndDefaultValues);
            case WORD -> wordTemplateProcessor.process(template.getContentBytes(), mergedParametersAndDefaultValues);
        };
    }

    byte[] renderDirectInternal(final DirectRenderRequest request) {
        var template = decodeBase64(request.getContent());

        return switch (getTemplateType(template)) {
            case PEBBLE -> pebbleTemplateProcessor.process(DelegatingLoader.DIRECT_PREFIX + request.getContent(), request.getParameters());
            case WORD -> wordTemplateProcessor.process(template, request.getParameters());
        };
    }

    byte[] renderHtmlAsPdf(final byte[] document) {
        try (var out = new ByteArrayOutputStream()) {
            iTextRenderer.setDocumentFromString(bytesToString(document));
            iTextRenderer.layout();
            iTextRenderer.createPDF(out);
            iTextRenderer.finishPDF();

            return out.toByteArray();
        } catch (IOException e) {
            throw new TemplateException("Unable to render PDF", e);
        }
    }

    byte[] renderWordAsPdf(final byte[] document) {
        try (var in = new ByteArrayInputStream(document);
             var doc = new XWPFDocument(in);
             var out = new ByteArrayOutputStream()) {
            PdfConverter.getInstance().convert(doc, out, PdfOptions.create());

            return out.toByteArray();
        } catch (IOException e) {
            throw new TemplateException("Unable to render PDF", e);
        }
    }
}
