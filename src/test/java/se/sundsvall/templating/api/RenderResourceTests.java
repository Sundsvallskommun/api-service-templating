package se.sundsvall.templating.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
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
import se.sundsvall.templating.service.RenderingService;

@ExtendWith(MockitoExtension.class)
class RenderResourceTests {

    @Mock
    private RenderingService mockRenderingService;

    private RenderResource resource;

    @BeforeEach
    void setUp() {
        resource = new RenderResource(mockRenderingService);
    }

    @Test
    void test_render() {
        when(mockRenderingService.renderTemplate(any(RenderRequest.class))).thenReturn("someText");

        var result = resource.render(new RenderRequest());
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().getOutput()).isEqualTo("someText");

        verify(mockRenderingService, times(1)).renderTemplate(any(RenderRequest.class));
    }

    @Test
    void test_renderAsPdf() {
        when(mockRenderingService.renderTemplateAsPdf(any(RenderRequest.class))).thenReturn("someText");

        var result = resource.renderPdf(new RenderRequest());
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().getOutput()).isEqualTo("someText");

        verify(mockRenderingService, times(1)).renderTemplateAsPdf(any(RenderRequest.class));
    }

    @Test
    void test_renderDirect() {
        when(mockRenderingService.renderDirect(any(DirectRenderRequest.class)))
            .thenReturn("someText");

        var result = resource.renderDirect(new DirectRenderRequest());
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().getOutput()).isEqualTo("someText");

        verify(mockRenderingService, times(1)).renderDirect(any(DirectRenderRequest.class));
    }
    
    @Test
    void test_renderDirectAsPdf() {
        when(mockRenderingService.renderDirectAsPdf(any(DirectRenderRequest.class)))
            .thenReturn("someText");

        var result = resource.renderDirectPdf(new DirectRenderRequest());
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().getOutput()).isEqualTo("someText");

        verify(mockRenderingService, times(1)).renderDirectAsPdf(any(DirectRenderRequest.class));
    }
}
