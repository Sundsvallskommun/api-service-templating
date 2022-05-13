package se.sundsvall.templating.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.NullNode;
import com.github.fge.jsonpatch.JsonPatch;
import com.github.fge.jsonpatch.JsonPatchException;
import com.mitchellbosecke.pebble.PebbleEngine;
import com.mitchellbosecke.pebble.template.PebbleTemplate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.zalando.problem.ThrowableProblem;

import se.sundsvall.templating.api.domain.DetailedTemplateResponse;
import se.sundsvall.templating.api.domain.RenderRequest;
import se.sundsvall.templating.api.domain.TemplateRequest;
import se.sundsvall.templating.api.domain.TemplateResponse;
import se.sundsvall.templating.integration.db.DbIntegration;
import se.sundsvall.templating.integration.db.entity.TemplateEntity;
import se.sundsvall.templating.service.mapper.TemplatingServiceMapper;

@ExtendWith(MockitoExtension.class)
class TemplatingServiceTests {

    @Mock
    private DbIntegration mockDbIntegration;
    @Mock
    private PebbleEngine mockPebbleEngine;
    @Mock
    private ObjectMapper mockObjectMapper;
    @Mock
    private TemplatingServiceMapper mockTemplatingServiceMapper;

    @Mock
    private PebbleTemplate mockPebbleTemplate;
    @Mock
    private RenderRequest mockRenderRequest;

    @Mock
    private JsonPatch mockJsonPatch;
    @Mock
    private TemplateEntity mockTemplateEntity;

    private TemplatingService service;

    @BeforeEach
    void setUp() {
        service = new TemplatingService(mockDbIntegration, mockPebbleEngine, mockObjectMapper,
            mockTemplatingServiceMapper);
    }

    @Test
    void test_renderTemplate() {
        when(mockRenderRequest.getTemplateIdentifier()).thenReturn("someTemplateId");
        when(mockTemplateEntity.getId()).thenReturn("someTemplateId");
        when(mockDbIntegration.getTemplate(any(String.class))).thenReturn(Optional.of(mockTemplateEntity));
        when(mockPebbleEngine.getTemplate(any(String.class))).thenReturn(mockPebbleTemplate);

        var result = service.renderTemplate(mockRenderRequest);
        assertThat(result).isNotNull();

        verify(mockRenderRequest, times(1)).getTemplateIdentifier();
        verify(mockRenderRequest, times(1)).getParameters();
        verify(mockDbIntegration, times(1)).getTemplate(any(String.class));
        verify(mockPebbleEngine, times(1)).getTemplate(any(String.class));
    }

    @Test
    void test_renderTemplate_whenRenderingFails() throws IOException {
        when(mockTemplateEntity.getId()).thenReturn("someTemplateId");
        when(mockRenderRequest.getTemplateIdentifier()).thenReturn("someTemplateId");
        when(mockDbIntegration.getTemplate(any(String.class))).thenReturn(Optional.of(mockTemplateEntity));
        when(mockPebbleEngine.getTemplate(any(String.class))).thenReturn(mockPebbleTemplate);
        doThrow(new IOException()).when(mockPebbleTemplate).evaluate(any(Writer.class), anyMap());

        assertThatExceptionOfType(RuntimeException.class)
            .isThrownBy(() -> service.renderTemplate(mockRenderRequest));
    }

    @Test
    void test_renderTemplate_whenTemplateDoesNotExist() {
        when(mockRenderRequest.getTemplateIdentifier()).thenReturn("someTemplateId");
        when(mockDbIntegration.getTemplate(any(String.class))).thenReturn(Optional.empty());

        assertThatExceptionOfType(ThrowableProblem.class)
            .isThrownBy(() -> service.renderTemplate(mockRenderRequest));

        verify(mockDbIntegration, times(1)).getTemplate(any(String.class));
        verify(mockRenderRequest, times(2)).getTemplateIdentifier();
    }

    @Test
    void test_getAllTemplates() {
        var templateEntities = List.of(TemplateEntity.builder().build(),
            TemplateEntity.builder().build());

        when(mockDbIntegration.getAllTemplates()).thenReturn(templateEntities);
        when(mockTemplatingServiceMapper.toTemplateResponse(any(TemplateEntity.class)))
            .thenReturn(TemplateResponse.builder().build());

        var templates = service.getAllTemplates();
        assertThat(templates).hasSize(templateEntities.size());

        verify(mockDbIntegration, times(1)).getAllTemplates();
        verify(mockTemplatingServiceMapper, times(templateEntities.size()))
            .toTemplateResponse(any(TemplateEntity.class));
    }

    @Test
    void test_getTemplate() {
        when(mockDbIntegration.getTemplate(any(String.class)))
            .thenReturn(Optional.of(TemplateEntity.builder().build()));
        when(mockTemplatingServiceMapper.toDetailedTemplateResponse(any(TemplateEntity.class)))
            .thenReturn(DetailedTemplateResponse.builder().build());

        var template = service.getTemplate("someTemplateId");
        assertThat(template).isPresent();

        verify(mockDbIntegration, times(1)).getTemplate(any(String.class));
        verify(mockTemplatingServiceMapper, times(1)).toDetailedTemplateResponse(any(TemplateEntity.class));
    }

    @Test
    void test_getTemplate_whenTemplateDoesNotExist() {
        when(mockDbIntegration.getTemplate(any(String.class))).thenReturn(Optional.empty());

        var template = service.getTemplate("someTemplateId");
        assertThat(template).isNotPresent();

        verify(mockDbIntegration, times(1)).getTemplate(any(String.class));
        verify(mockTemplatingServiceMapper, never()).toTemplateResponse(any(TemplateEntity.class));
    }

    @Test
    void test_saveTemplate() {
        when(mockTemplatingServiceMapper.toTemplateEntity(any(TemplateRequest.class)))
            .thenReturn(TemplateEntity.builder().build());
        when(mockTemplatingServiceMapper.toTemplateResponse(any(TemplateEntity.class)))
            .thenReturn(TemplateResponse.builder().build());
        when(mockDbIntegration.saveTemplate(any(TemplateEntity.class)))
            .thenReturn(TemplateEntity.builder().build());

        service.saveTemplate(new TemplateRequest());

        verify(mockTemplatingServiceMapper, times(1)).toTemplateEntity(any(TemplateRequest.class));
        verify(mockTemplatingServiceMapper, times(1)).toTemplateResponse(any(TemplateEntity.class));
        verify(mockDbIntegration, times(1)).saveTemplate(any(TemplateEntity.class));
    }

    @Test
    void test_updateTemplate() throws JsonProcessingException, JsonPatchException {
        when(mockDbIntegration.getTemplate(any(String.class)))
            .thenReturn(Optional.of(TemplateEntity.builder().build()));
        when(mockObjectMapper.convertValue(any(TemplateEntity.class), eq(JsonNode.class))).thenReturn(NullNode.getInstance());
        when(mockObjectMapper.treeToValue(any(TreeNode.class), eq(TemplateEntity.class)))
            .thenReturn(TemplateEntity.builder().build());
        when(mockDbIntegration.saveTemplate(any(TemplateEntity.class)))
            .thenReturn(TemplateEntity.builder().build());
        when(mockTemplatingServiceMapper.toTemplateResponse(any(TemplateEntity.class)))
            .thenReturn(TemplateResponse.builder().build());
        when(mockJsonPatch.apply(any(JsonNode.class))).thenReturn(NullNode.getInstance());

        var response = service.updateTemplate("someTemplateId", mockJsonPatch);
        assertThat(response).isNotNull();

        verify(mockDbIntegration, times(1)).getTemplate(any(String.class));
        verify(mockObjectMapper, times(1)).convertValue(any(TemplateEntity.class), eq(JsonNode.class));
        verify(mockObjectMapper, times(1)).treeToValue(any(TreeNode.class), eq(TemplateEntity.class));
        verify(mockDbIntegration, times(1)).saveTemplate(any(TemplateEntity.class));
        verify(mockTemplatingServiceMapper, times(1)).toTemplateResponse(any(TemplateEntity.class));
        verify(mockJsonPatch, times(1)).apply(any(JsonNode.class));
    }

    @Test
    void test_updateTemplate_whenPatchFails() throws JsonPatchException {
        when(mockDbIntegration.getTemplate(any(String.class)))
            .thenReturn(Optional.of(TemplateEntity.builder().build()));
        when(mockObjectMapper.convertValue(any(TemplateEntity.class), eq(JsonNode.class))).thenReturn(NullNode.getInstance());
        when(mockJsonPatch.apply(any(JsonNode.class))).thenThrow(new JsonPatchException("Dummy"));

        assertThatExceptionOfType(IllegalStateException.class)
            .isThrownBy(() -> service.updateTemplate("someTemplateId", mockJsonPatch));

        verify(mockDbIntegration, times(1)).getTemplate(any(String.class));
    }

    @Test
    void test_updateTemplate_whenTemplateDoesNotExist() {
        when(mockDbIntegration.getTemplate(any(String.class))).thenReturn(Optional.empty());

        assertThatExceptionOfType(ThrowableProblem.class)
            .isThrownBy(() -> service.updateTemplate("someTemplateId", mockJsonPatch));
    }

    @Test
    void test_deleteTemplate() {
        service.deleteTemplate("someTemplateId");

        verify(mockDbIntegration, times(1)).deleteTemplate(any(String.class));
    }
}
