package se.sundsvall.templating.service;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static se.sundsvall.templating.domain.TemplateType.PEBBLE;
import static se.sundsvall.templating.domain.TemplateType.WORD;

import com.itextpdf.html2pdf.HtmlConverter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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

@ExtendWith(MockitoExtension.class)
class RenderingServiceTests {

	private static final String MUNICIPALITY_ID = "municipalityId";
	private static final String IDENTIFIER = "someTemplateId";

	@Mock
	private PebbleProperties mockPebbleProperties;

	@Mock
	private PebbleTemplateProcessor mockPebbleTemplateProcessor;

	@Mock
	private WordTemplateProcessor mockWordTemplateProcessor;

	@Mock
	private DbIntegration mockDbIntegration;

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

		verify(mockRenderRequest).getIdentifier();
		verify(mockRenderRequest).getParameters();
		verify(mockDbIntegration).getTemplate(MUNICIPALITY_ID, IDENTIFIER, null);
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

		verify(mockDbIntegration).getTemplate(MUNICIPALITY_ID, IDENTIFIER, "1.8");
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
		var document = "someTemplateContent".getBytes(UTF_8);

		try (var mockHtmlConverter = mockStatic(HtmlConverter.class)) {
			service.renderHtmlAsPdf(document);

			mockHtmlConverter.verify(() -> HtmlConverter.convertToPdf(any(String.class), any(ByteArrayOutputStream.class)));
		}
	}

	@Test
	void renderHtmlAsPdf_whenExceptionIsThrown() {
		var document = "someTemplateContent".getBytes(UTF_8);

		try (var mockHtmlConverter = mockStatic(HtmlConverter.class)) {
			mockHtmlConverter.when(() -> HtmlConverter.convertToPdf(any(String.class), any(ByteArrayOutputStream.class))).thenAnswer(_ -> {
				throw new IOException("dummy");
			});

			assertThatExceptionOfType(TemplateException.class)
				.isThrownBy(() -> service.renderHtmlAsPdf(document))
				.withMessage("Unable to render PDF");

			mockHtmlConverter.verify(() -> HtmlConverter.convertToPdf(any(String.class), any(ByteArrayOutputStream.class)));
		}
	}
}
