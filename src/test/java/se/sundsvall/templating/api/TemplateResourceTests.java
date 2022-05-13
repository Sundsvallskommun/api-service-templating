package se.sundsvall.templating.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import se.sundsvall.templating.api.domain.DetailedTemplateResponse;
import se.sundsvall.templating.api.domain.TemplateRequest;
import se.sundsvall.templating.api.domain.TemplateResponse;
import se.sundsvall.templating.service.TemplatingService;

@ExtendWith(MockitoExtension.class)
class TemplateResourceTests {

    @Mock
    private TemplatingService mockTemplatingService;
    @Mock
    private TemplateResponse mockTemplateResponse;

    private TemplateResource resource;

    @BeforeEach
    void setUp() {
        resource = new TemplateResource(mockTemplatingService);
    }

    @Test
    void test_getAll() {
        when(mockTemplatingService.getAllTemplates()).thenReturn(List.of(mockTemplateResponse));

        var result = resource.getAllTemplates();
        assertThat(result).hasSize(1);

        verify(mockTemplatingService, times(1)).getAllTemplates();
    }

    @Test
    void test_getTemplate() {
        when(mockTemplatingService.getTemplate(any(String.class))).thenReturn(Optional.of(DetailedTemplateResponse.builder().build()));

        var result = resource.getTemplate("someTemplateId");
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isNotNull();

        verify(mockTemplatingService, times(1)).getTemplate(any(String.class));
    }

    @Test
    void test_getTemplate_whenTemplateDoesNotExist() {
        when(mockTemplatingService.getTemplate(any(String.class))).thenReturn(Optional.empty());

        var result = resource.getTemplate("someTemplateId");
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(result.getBody()).isNull();

        verify(mockTemplatingService, times(1)).getTemplate(any(String.class));
    }

    @Test
    void test_saveTemplate() {
        when(mockTemplatingService.saveTemplate(any(TemplateRequest.class)))
            .thenReturn(mockTemplateResponse);
        when(mockTemplateResponse.getIdentifier()).thenReturn("someId");

        var result = resource.saveTemplate(new TemplateRequest());
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(result.getBody()).isNotNull();

        verify(mockTemplatingService, times(1)).saveTemplate(any(TemplateRequest.class));
    }

    @Test
    void test_updateTemplate() {
        when(mockTemplatingService.updateTemplate(any(String.class), any()))
            .thenReturn(mockTemplateResponse);

        var result = resource.updateTemplate("someTemplateId", null);
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isNotNull();

        verify(mockTemplatingService, times(1)).updateTemplate(any(String.class), any());
    }

    @Test
    void test_deleteTemplate() {
        resource.deleteTemplate("someTemplateId");

        verify(mockTemplatingService, times(1)).deleteTemplate(any(String.class));
    }
}
