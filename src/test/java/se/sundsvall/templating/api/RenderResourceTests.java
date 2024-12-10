package se.sundsvall.templating.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import se.sundsvall.templating.api.domain.DirectRenderRequest;
import se.sundsvall.templating.api.domain.RenderRequest;
import se.sundsvall.templating.domain.ContextMunicipalityId;
import se.sundsvall.templating.service.RenderingService;

@ExtendWith(MockitoExtension.class)
class RenderResourceTests {

	private static final String MUNICIPALITY_ID = "2281";

	@Mock
	private RenderingService mockRenderingService;

	@Mock
	private ContextMunicipalityId mockContextMunicipalityId;

	private RenderResource resource;

	@BeforeEach
	void setUp() {
		resource = new RenderResource(mockContextMunicipalityId, mockRenderingService);
	}

	@Test
	void test_render() {
		when(mockRenderingService.renderTemplate(any(), any(RenderRequest.class))).thenReturn("someText");

		var result = resource.render(MUNICIPALITY_ID, new RenderRequest());
		assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(result.getBody()).isNotNull();
		assertThat(result.getBody().getOutput()).isEqualTo("someText");

		verify(mockRenderingService, times(1)).renderTemplate(eq(MUNICIPALITY_ID), any(RenderRequest.class));
	}

	@Test
	void test_renderAsPdf() {
		when(mockRenderingService.renderTemplateAsPdf(any(), any(RenderRequest.class))).thenReturn("someText");

		var result = resource.renderPdf(MUNICIPALITY_ID, new RenderRequest());
		assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(result.getBody()).isNotNull();
		assertThat(result.getBody().getOutput()).isEqualTo("someText");

		verify(mockRenderingService, times(1)).renderTemplateAsPdf(eq(MUNICIPALITY_ID), any(RenderRequest.class));
	}

	@Test
	void test_renderDirect() {
		when(mockRenderingService.renderDirect(any(DirectRenderRequest.class)))
			.thenReturn("someText");

		var result = resource.renderDirect(MUNICIPALITY_ID, new DirectRenderRequest());
		assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(result.getBody()).isNotNull();
		assertThat(result.getBody().getOutput()).isEqualTo("someText");

		verify(mockRenderingService, times(1)).renderDirect(any(DirectRenderRequest.class));
	}

	@Test
	void test_renderDirectAsPdf() {
		when(mockRenderingService.renderDirectAsPdf(any(DirectRenderRequest.class)))
			.thenReturn("someText");

		var result = resource.renderDirectPdf(MUNICIPALITY_ID, new DirectRenderRequest());
		assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(result.getBody()).isNotNull();
		assertThat(result.getBody().getOutput()).isEqualTo("someText");

		verify(mockRenderingService, times(1)).renderDirectAsPdf(any(DirectRenderRequest.class));
	}
}
