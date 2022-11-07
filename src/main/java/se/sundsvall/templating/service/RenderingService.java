package se.sundsvall.templating.service;

import static java.lang.String.format;
import static java.util.stream.Collectors.toMap;

import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

import com.mitchellbosecke.pebble.PebbleEngine;
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
import se.sundsvall.templating.util.BASE64;

@Service
public class RenderingService {

    private final PebbleEngine pebbleEngine;
    private final ITextRenderer iTextRenderer;
    private final PebbleProperties pebbleProperties;
    private final DbIntegration dbIntegration;

    public RenderingService(final PebbleEngine pebbleEngine, final ITextRenderer iTextRenderer,
            final PebbleProperties pebbleProperties, final DbIntegration dbIntegration) {
        this.pebbleEngine = pebbleEngine;
        this.iTextRenderer = iTextRenderer;
        this.pebbleProperties = pebbleProperties;
        this.dbIntegration = dbIntegration;
    }

    public String renderTemplate(final RenderRequest request) {
        return renderTemplateInternal(request, true);
    }

    public String renderTemplateAsPdf(final RenderRequest request) {
        var renderedTemplate = renderTemplateInternal(request, false);
        var renderedPdf = renderHtmlAsPdf(renderedTemplate);

        // Encode the output as BASE64
        return BASE64.encode(renderedPdf);
    }

    public String renderDirect(final DirectRenderRequest request) {
        return renderDirectInternal(request, true);
    }

    public String renderDirectAsPdf(final DirectRenderRequest request) {
        var renderedTemplate = renderDirectInternal(request, false);
        var renderedPdf = renderHtmlAsPdf(renderedTemplate);

        return BASE64.encode(renderedPdf);
    }

    String renderTemplateInternal(final RenderRequest request, final boolean base64EncodeOutput) {
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

        try (var writer = new StringWriter()) {
            pebbleEngine.getTemplate(template.getIdentifier() + ":" + template.getVersion())
                .evaluate(writer, mergedParametersAndDefaultValues);

            // Optionally encode the output as BASE64
            return base64EncodeOutput ? BASE64.encode(writer.toString()) : writer.toString();
        } catch (Exception e) {
            throw new TemplateException(e);
        }
    }

    String renderDirectInternal(final DirectRenderRequest request, final boolean base64EncodeOutput) {
        try (var writer = new StringWriter()) {
            pebbleEngine.getTemplate(DelegatingLoader.DIRECT_PREFIX + request.getContent())
                .evaluate(writer, request.getParameters());

            var output = writer.toString().substring(DelegatingLoader.DIRECT_PREFIX.length());

            // Optionally encode the output as BASE64
            return base64EncodeOutput ? BASE64.encode(output) : output;
        } catch (Exception e) {
            throw new TemplateException(e);
        }
    }

    byte[] renderHtmlAsPdf(final String html) {
        var os = new ByteArrayOutputStream();

        var document = Jsoup.parse(html, StandardCharsets.UTF_8.name());
        document.outputSettings().syntax(Document.OutputSettings.Syntax.xml);

        iTextRenderer.setDocumentFromString(document.html());
        iTextRenderer.layout();
        iTextRenderer.createPDF(os);
        iTextRenderer.finishPDF();

        return os.toByteArray();
    }
}
