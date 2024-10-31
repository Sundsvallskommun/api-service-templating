package se.sundsvall.templating.service;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static se.sundsvall.templating.domain.TemplateType.PEBBLE;
import static se.sundsvall.templating.domain.TemplateType.WORD;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.xhtmlrenderer.pdf.ITextRenderer;
import org.zalando.problem.ThrowableProblem;

import se.sundsvall.templating.api.domain.DirectRenderRequest;
import se.sundsvall.templating.api.domain.RenderRequest;
import se.sundsvall.templating.configuration.properties.PebbleProperties;
import se.sundsvall.templating.domain.TemplateType;
import se.sundsvall.templating.exception.TemplateException;
import se.sundsvall.templating.integration.db.DbIntegration;
import se.sundsvall.templating.integration.db.entity.TemplateEntity;
import se.sundsvall.templating.service.processor.PebbleTemplateProcessor;
import se.sundsvall.templating.service.processor.WordTemplateProcessor;
import se.sundsvall.templating.util.TemplateUtil;

import io.pebbletemplates.pebble.template.PebbleTemplate;

@ExtendWith(MockitoExtension.class)
class RenderingServiceTests {

	private static final String MUNICIPALITY_ID = "municipalityId";
	public static final String IDENTIFIER = "someTemplateId";

	@Mock
	private ITextRenderer mockITextRenderer;
	@Mock
	private PebbleProperties mockPebbleProperties;
	@Mock
	private PebbleTemplateProcessor mockPebbleTemplateProcessor;
	@Mock
	private WordTemplateProcessor mockWordTemplateProcessor;
	@Mock
	private DbIntegration mockDbIntegration;

	@Mock
	private PebbleTemplate mockPebbleTemplate;
	@Mock
	private RenderRequest mockRenderRequest;
	@Mock
	private DirectRenderRequest mockDirectRenderRequest;

	@Mock
	private TemplateEntity mockTemplateEntity;

	@InjectMocks
	private RenderingService service;

	@Test
	void renderTemplate() {
		when(mockRenderRequest.getIdentifier()).thenReturn(IDENTIFIER);
		when(mockTemplateEntity.getIdentifier()).thenReturn(IDENTIFIER);
		when(mockTemplateEntity.getType()).thenReturn(PEBBLE);
		when(mockDbIntegration.getTemplate(any(), any(), any()))
			.thenReturn(Optional.of(mockTemplateEntity));
		when(mockPebbleTemplateProcessor.process(any(String.class), anyMap())).thenReturn("someResult".getBytes(UTF_8));

		var result = service.renderTemplate(MUNICIPALITY_ID, mockRenderRequest);
		assertThat(result).isNotNull();

		verify(mockRenderRequest, times(1)).getIdentifier();
		verify(mockRenderRequest, times(1)).getParameters();
		verify(mockDbIntegration, times(1)).getTemplate(MUNICIPALITY_ID, IDENTIFIER, null);
		verify(mockPebbleTemplateProcessor).process(any(String.class), anyMap());
		verifyNoInteractions(mockWordTemplateProcessor);
	}

	@Test
	void renderTemplate_whenRenderingFails() {
		when(mockTemplateEntity.getIdentifier()).thenReturn(IDENTIFIER);
		when(mockRenderRequest.getIdentifier()).thenReturn(IDENTIFIER);
		when(mockDbIntegration.getTemplate(any(), any(), any())).thenReturn(Optional.of(mockTemplateEntity));
		when(mockTemplateEntity.getType()).thenReturn(PEBBLE);
		doThrow(new TemplateException(new IOException())).when(mockPebbleTemplateProcessor).process(any(), anyMap());

		assertThatExceptionOfType(RuntimeException.class)
			.isThrownBy(() -> service.renderTemplate(MUNICIPALITY_ID, mockRenderRequest));
	}

	@Test
	void renderTemplate_whenTemplateDoesNotExist() {
		when(mockRenderRequest.getIdentifier()).thenReturn(IDENTIFIER);
		when(mockRenderRequest.getVersion()).thenReturn("1.8");
		when(mockDbIntegration.getTemplate(any(), any(), any())).thenReturn(Optional.empty());

		assertThatExceptionOfType(ThrowableProblem.class)
			.isThrownBy(() -> service.renderTemplate(MUNICIPALITY_ID, mockRenderRequest));

		verify(mockDbIntegration, times(1)).getTemplate(MUNICIPALITY_ID, IDENTIFIER, "1.8");
		verify(mockRenderRequest, times(3)).getIdentifier();
	}

	@ParameterizedTest
	@EnumSource(TemplateType.class)
	void renderDirectInternal(final TemplateType templateType) {
		var result = "someResult".getBytes(UTF_8);

		when(mockDirectRenderRequest.getContent()).thenReturn("bG9yZW0gaXBzdW0=");

		try (var mockTemplateUtil = mockStatic(TemplateUtil.class)) {
			mockTemplateUtil.when(() -> TemplateUtil.decodeBase64(any(String.class))).thenCallRealMethod();
			mockTemplateUtil.when(() -> TemplateUtil.getTemplateType(any(byte[].class))).thenReturn(templateType);

			if (templateType == WORD) {
				when(mockWordTemplateProcessor.process(any(byte[].class), anyMap())).thenReturn(result);

				assertThat(service.renderDirectInternal(mockDirectRenderRequest)).isEqualTo(result);

				verify(mockWordTemplateProcessor).process(any(byte[].class), anyMap());
				verifyNoMoreInteractions(mockWordTemplateProcessor);
				verifyNoInteractions(mockPebbleTemplateProcessor);
			} else {
				when(mockPebbleTemplateProcessor.process(any(String.class), anyMap())).thenReturn(result);

				assertThat(service.renderDirectInternal(mockDirectRenderRequest)).isEqualTo(result);

				verify(mockPebbleTemplateProcessor).process(any(String.class), anyMap());
				verifyNoMoreInteractions(mockPebbleTemplateProcessor);
				verifyNoInteractions(mockWordTemplateProcessor);
			}

			mockTemplateUtil.verify(() -> TemplateUtil.decodeBase64(any(String.class)));
			mockTemplateUtil.verify(() -> TemplateUtil.getTemplateType(any(byte[].class)));
			mockTemplateUtil.verifyNoMoreInteractions();
		}
	}

	@Test
	void renderHtmlAsPdf() {
		var document = "someTemplateContent";

		service.renderHtmlAsPdf(document.getBytes(UTF_8));

		verify(mockITextRenderer, times(1)).setDocumentFromString("<html>\n <head></head>\n <body>\n  " + document + "\n </body>\n</html>");
		verify(mockITextRenderer, times(1)).layout();
		verify(mockITextRenderer, times(1)).createPDF(any(OutputStream.class));
		verify(mockITextRenderer, times(1)).finishPDF();
	}

	@Test
	void renderHtmlAsPdf_whenExceptionIsThrown() {
		var template = "someTemplateContent".getBytes(UTF_8);

		doAnswer(invocation -> { throw new IOException("dummy"); }).when(mockITextRenderer).createPDF(any(OutputStream.class));

		assertThatExceptionOfType(TemplateException.class)
			.isThrownBy(() -> service.renderHtmlAsPdf(template))
			.withMessage("Unable to render PDF");
	}
}
