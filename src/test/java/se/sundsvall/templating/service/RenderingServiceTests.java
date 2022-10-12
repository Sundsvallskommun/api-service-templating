package se.sundsvall.templating.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.util.Optional;

import com.mitchellbosecke.pebble.PebbleEngine;
import com.mitchellbosecke.pebble.template.PebbleTemplate;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.xhtmlrenderer.pdf.ITextRenderer;
import org.zalando.problem.ThrowableProblem;

import se.sundsvall.templating.api.domain.RenderRequest;
import se.sundsvall.templating.configuration.properties.PebbleProperties;
import se.sundsvall.templating.integration.db.DbIntegration;
import se.sundsvall.templating.integration.db.entity.TemplateEntity;

@ExtendWith(MockitoExtension.class)
class RenderingServiceTests {

    @Mock
    private PebbleEngine mockPebbleEngine;
    @Mock
    private ITextRenderer mockITextRenderer;
    @Mock
    private PebbleProperties mockPebbleProperties;
    @Mock
    private DbIntegration mockDbIntegration;

    @Mock
    private PebbleTemplate mockPebbleTemplate;
    @Mock
    private RenderRequest mockRenderRequest;

    @Mock
    private TemplateEntity mockTemplateEntity;

    private RenderingService service;

    @BeforeEach
    void setUp() {
        service = new RenderingService(mockPebbleEngine, mockITextRenderer, mockPebbleProperties, mockDbIntegration);
    }

    @Test
    void test_renderTemplate() {
        when(mockRenderRequest.getIdentifier()).thenReturn("someTemplateId");
        when(mockTemplateEntity.getIdentifier()).thenReturn("someTemplateId");
        when(mockDbIntegration.getTemplate(any(String.class), nullable(String.class)))
            .thenReturn(Optional.of(mockTemplateEntity));
        when(mockPebbleEngine.getTemplate(any(String.class))).thenReturn(mockPebbleTemplate);

        var result = service.renderTemplate(mockRenderRequest);
        assertThat(result).isNotNull();

        verify(mockRenderRequest, times(1)).getIdentifier();
        verify(mockRenderRequest, times(1)).getParameters();
        verify(mockDbIntegration, times(1)).getTemplate(any(String.class), nullable(String.class));
        verify(mockPebbleEngine, times(1)).getTemplate(any(String.class));
    }

    @Test
    void test_renderTemplate_whenRenderingFails() throws IOException {
        when(mockTemplateEntity.getId()).thenReturn("someTemplateId");
        when(mockRenderRequest.getIdentifier()).thenReturn("someTemplateId");
        when(mockDbIntegration.getTemplate(any(String.class), any(String.class))).thenReturn(Optional.of(mockTemplateEntity));
        when(mockPebbleEngine.getTemplate(any(String.class))).thenReturn(mockPebbleTemplate);
        doThrow(new IOException()).when(mockPebbleTemplate).evaluate(any(Writer.class), anyMap());

        assertThatExceptionOfType(RuntimeException.class)
            .isThrownBy(() -> service.renderTemplate(mockRenderRequest));
    }

    @Test
    void test_renderTemplate_whenTemplateDoesNotExist() {
        when(mockRenderRequest.getIdentifier()).thenReturn("someTemplateId");
        when(mockRenderRequest.getVersion()).thenReturn("1.8");
        when(mockDbIntegration.getTemplate(any(String.class), any(String.class))).thenReturn(Optional.empty());

        assertThatExceptionOfType(ThrowableProblem.class)
            .isThrownBy(() -> service.renderTemplate(mockRenderRequest));

        verify(mockDbIntegration, times(1)).getTemplate(any(String.class), any(String.class));
        verify(mockRenderRequest, times(3)).getIdentifier();
    }

    @Test
    void test_renderHtmlAsPdf() {
        var mockOutputSettings = mock(Document.OutputSettings.class);
        var mockDocument = mock(Document.class);

        when(mockDocument.html()).thenReturn("someHtml");
        when(mockDocument.outputSettings()).thenReturn(mockOutputSettings);

        try (MockedStatic<Jsoup> mockJsoup = mockStatic(Jsoup.class)) {
            mockJsoup.when(((() -> Jsoup.parse(any(String.class), any(String.class)))))
                .thenReturn(mockDocument);

            service.renderHtmlAsPdf("someHtml");
        }

        verify(mockITextRenderer, times(1)).setDocumentFromString(any(String.class));
        verify(mockITextRenderer, times(1)).layout();
        verify(mockITextRenderer, times(1)).createPDF(any(OutputStream.class));
        verify(mockITextRenderer, times(1)).finishPDF();

        verify(mockDocument, times(1)).html();
        verify(mockDocument, times(1)).outputSettings();
        verify(mockOutputSettings, times(1)).syntax(any(Document.OutputSettings.Syntax.class));
    }
}
