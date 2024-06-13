package se.sundsvall.templating.service;

import static java.lang.String.format;
import static java.util.Optional.ofNullable;
import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.toMap;
import static se.sundsvall.templating.util.TemplateUtil.bytesToString;
import static se.sundsvall.templating.util.TemplateUtil.decodeBase64;
import static se.sundsvall.templating.util.TemplateUtil.encodeBase64;
import static se.sundsvall.templating.util.TemplateUtil.getTemplateType;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.AbstractMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.tomcat.util.http.fileupload.ByteArrayOutputStream;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
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

    private static final String BASE64_VALUE_PREFIX = "BASE64:";

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
        var template = ofNullable(request.getIdentifier())
            .flatMap(identifier -> dbIntegration.getTemplate(identifier, request.getVersion()))
            .orElseGet(() -> ofNullable(request.getMetadata())
                .filter(not(List::isEmpty))
                .flatMap(dbIntegration::findTemplate)
                .orElse(null));

        if (null == template) {
            var message = ofNullable(request.getIdentifier())
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
        // Decode request parameters
        var decodedRequestParameters = decodeRequestParameters(request.getParameters());
        // Add request parameters
        mergedParametersAndDefaultValues.putAll(decodedRequestParameters);

        // Process the template
        return switch (template.getType()) {
            case PEBBLE -> pebbleTemplateProcessor.process(template.getIdentifier() + ":" + template.getVersion(), mergedParametersAndDefaultValues);
            case WORD -> wordTemplateProcessor.process(decodeBase64(template.getContent()), mergedParametersAndDefaultValues);
        };
    }

    byte[] renderDirectInternal(final DirectRenderRequest request) {
        var template = decodeBase64(request.getContent());

        // Decode request parameters
        var decodedRequestParameters = decodeRequestParameters(request.getParameters());

        return switch (getTemplateType(template)) {
            case PEBBLE -> pebbleTemplateProcessor.process(DelegatingLoader.DIRECT_PREFIX + request.getContent(), decodedRequestParameters);
            case WORD -> wordTemplateProcessor.process(template, decodedRequestParameters);
        };
    }

    byte[] renderHtmlAsPdf(final byte[] document) {
        try (var out = new ByteArrayOutputStream()) {
            // Run the document through Jsoup to wrap it in a proper HTML/XML document in order to
            // get OpenPDF to play nice
            var doc = Jsoup.parse(bytesToString(document), "UTF-8");
            doc.outputSettings().syntax(Document.OutputSettings.Syntax.xml);

            iTextRenderer.setDocumentFromString(doc.html());
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

    /**
     * Decodes any parameter value that is a string and is prefixed with "BASE64:".
     *
     * @param requestParameters the original request parameters
     * @return the request parameters with any BASE64-encoded values decoded
     */
    Map<String, Object> decodeRequestParameters(final Map<String, Object> requestParameters) {
        return ofNullable(requestParameters)
            .map(parameters -> parameters.entrySet().stream()
                .map(entry -> {
                    var value = entry.getValue();
                    if (value instanceof String stringValue && stringValue.startsWith(BASE64_VALUE_PREFIX)) {
                        value = bytesToString(decodeBase64(stringValue.substring(BASE64_VALUE_PREFIX.length())));
                    }
                    return new AbstractMap.SimpleEntry<>(entry.getKey(), value);
                })
                .collect(toMap(Map.Entry::getKey, Map.Entry::getValue))).orElse(Map.of());
    }
}
