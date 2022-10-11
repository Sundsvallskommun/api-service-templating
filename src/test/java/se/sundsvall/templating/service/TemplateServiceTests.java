package se.sundsvall.templating.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.NullNode;
import com.github.fge.jsonpatch.JsonPatch;
import com.github.fge.jsonpatch.JsonPatchException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;
import org.zalando.problem.ThrowableProblem;

import se.sundsvall.templating.api.domain.DetailedTemplateResponse;
import se.sundsvall.templating.api.domain.TemplateRequest;
import se.sundsvall.templating.api.domain.TemplateResponse;
import se.sundsvall.templating.api.domain.filter.FilterSpecifications;
import se.sundsvall.templating.domain.KeyValue;
import se.sundsvall.templating.integration.db.DbIntegration;
import se.sundsvall.templating.integration.db.entity.TemplateEntity;
import se.sundsvall.templating.service.mapper.TemplateMapper;

@ExtendWith(MockitoExtension.class)
class TemplateServiceTests {

    @Mock
    private ObjectMapper mockObjectMapper;
    @Mock
    private DbIntegration mockDbIntegration;
    @Mock
    private TemplateMapper mockTemplateMapper;

    @Mock
    private JsonPatch mockJsonPatch;

    private TemplateService service;

    @BeforeEach
    void setUp() {
        service = new TemplateService(mockObjectMapper, mockDbIntegration, mockTemplateMapper);
    }

    @Test
    void test_getAllTemplatesUsingFilterSpecification() {
        var templateEntities = List.of(TemplateEntity.builder().build(),
            TemplateEntity.builder().build(), TemplateEntity.builder().build());

        when(mockDbIntegration.findTemplates(ArgumentMatchers.<Specification<TemplateEntity>>any()))
            .thenReturn(templateEntities);
        when(mockTemplateMapper.toTemplateResponse(any(TemplateEntity.class)))
            .thenReturn(TemplateResponse.builder().build());

        var templates = service.getTemplates(FilterSpecifications.createEmptySpecification(TemplateEntity.class));
        assertThat(templates).hasSize(templateEntities.size());

        verify(mockDbIntegration, times(1)).findTemplates(ArgumentMatchers.<Specification<TemplateEntity>>any());
        verify(mockTemplateMapper, times(templateEntities.size()))
            .toTemplateResponse(any(TemplateEntity.class));
    }

    @Test
    void test_getAllTemplatesUsingEmptyKeyValueList() {
        var templateEntities = List.of(TemplateEntity.builder().build(),
            TemplateEntity.builder().build());

        when(mockDbIntegration.getAllTemplates()).thenReturn(templateEntities);
        when(mockTemplateMapper.toTemplateResponse(any(TemplateEntity.class)))
            .thenReturn(TemplateResponse.builder().build());

        var templates = service.getTemplates(List.of());
        assertThat(templates).hasSize(templateEntities.size());

        verify(mockDbIntegration, times(1)).getAllTemplates();
        verify(mockTemplateMapper, times(templateEntities.size()))
            .toTemplateResponse(any(TemplateEntity.class));
    }

    @Test
    void test_getAllTemplatesUsingKeyValueList() {
        var templateEntities = List.of(TemplateEntity.builder().build(),
            TemplateEntity.builder().build());

        when(mockDbIntegration.findTemplates(ArgumentMatchers.<List<KeyValue>>any()))
            .thenReturn(templateEntities);
        when(mockTemplateMapper.toTemplateResponse(any(TemplateEntity.class)))
            .thenReturn(TemplateResponse.builder().build());

        var templates = service.getTemplates(List.of(KeyValue.of("someKey", "someValue")));
        assertThat(templates).hasSize(templateEntities.size());

        verify(mockDbIntegration, times(1)).findTemplates(ArgumentMatchers.<List<KeyValue>>any());
        verify(mockTemplateMapper, times(templateEntities.size()))
            .toTemplateResponse(any(TemplateEntity.class));
    }

    @Test
    void test_getTemplate() {
        when(mockDbIntegration.getTemplate(any(String.class), any(String.class)))
            .thenReturn(Optional.of(TemplateEntity.builder().build()));
        when(mockTemplateMapper.toDetailedTemplateResponse(any(TemplateEntity.class)))
            .thenReturn(DetailedTemplateResponse.builder().build());

        var template = service.getTemplate("someTemplateId", "2.0");
        assertThat(template).isPresent();

        verify(mockDbIntegration, times(1)).getTemplate(any(String.class), any(String.class));
        verify(mockTemplateMapper, times(1)).toDetailedTemplateResponse(any(TemplateEntity.class));
    }

    @Test
    void test_getTemplate_whenTemplateDoesNotExist() {
        when(mockDbIntegration.getTemplate(any(String.class), any(String.class))).thenReturn(Optional.empty());

        var template = service.getTemplate("someTemplateId", "1.4");
        assertThat(template).isNotPresent();

        verify(mockDbIntegration, times(1)).getTemplate(any(String.class), any(String.class));
        verify(mockTemplateMapper, never()).toTemplateResponse(any(TemplateEntity.class));
    }

    @Test
    void test_saveTemplate() {
        when(mockTemplateMapper.toTemplateEntity(any(TemplateRequest.class)))
            .thenReturn(TemplateEntity.builder().build());
        when(mockTemplateMapper.toTemplateResponse(any(TemplateEntity.class)))
            .thenReturn(TemplateResponse.builder().build());
        when(mockDbIntegration.saveTemplate(any(TemplateEntity.class)))
            .thenReturn(TemplateEntity.builder().build());

        service.saveTemplate(new TemplateRequest());

        verify(mockTemplateMapper, times(1)).toTemplateEntity(any(TemplateRequest.class));
        verify(mockTemplateMapper, times(1)).toTemplateResponse(any(TemplateEntity.class));
        verify(mockDbIntegration, times(1)).saveTemplate(any(TemplateEntity.class));
    }

    @Test
    void test_updateTemplate() throws JsonProcessingException, JsonPatchException {
        when(mockDbIntegration.getTemplate(any(String.class), any(String.class)))
            .thenReturn(Optional.of(TemplateEntity.builder().build()));
        when(mockObjectMapper.convertValue(any(TemplateEntity.class), eq(JsonNode.class))).thenReturn(NullNode.getInstance());
        when(mockObjectMapper.treeToValue(any(TreeNode.class), eq(TemplateEntity.class)))
            .thenReturn(TemplateEntity.builder().build());
        when(mockDbIntegration.saveTemplate(any(TemplateEntity.class)))
            .thenReturn(TemplateEntity.builder().build());
        when(mockTemplateMapper.toTemplateResponse(any(TemplateEntity.class)))
            .thenReturn(TemplateResponse.builder().build());
        when(mockJsonPatch.apply(any(JsonNode.class))).thenReturn(NullNode.getInstance());

        var response = service.updateTemplate("someTemplateId", "1.5", mockJsonPatch);
        assertThat(response).isNotNull();

        verify(mockDbIntegration, times(1)).getTemplate(any(String.class), any(String.class));
        verify(mockObjectMapper, times(1)).convertValue(any(TemplateEntity.class), eq(JsonNode.class));
        verify(mockObjectMapper, times(1)).treeToValue(any(TreeNode.class), eq(TemplateEntity.class));
        verify(mockDbIntegration, times(1)).saveTemplate(any(TemplateEntity.class));
        verify(mockTemplateMapper, times(1)).toTemplateResponse(any(TemplateEntity.class));
        verify(mockJsonPatch, times(1)).apply(any(JsonNode.class));
    }

    @Test
    void test_updateTemplate_whenPatchFails() throws JsonPatchException {
        when(mockDbIntegration.getTemplate(any(String.class), any(String.class)))
            .thenReturn(Optional.of(TemplateEntity.builder().build()));
        when(mockObjectMapper.convertValue(any(TemplateEntity.class), eq(JsonNode.class))).thenReturn(NullNode.getInstance());
        when(mockJsonPatch.apply(any(JsonNode.class))).thenThrow(new JsonPatchException("Dummy"));

        assertThatExceptionOfType(IllegalStateException.class)
            .isThrownBy(() -> service.updateTemplate("someTemplateId", "1.1", mockJsonPatch));

        verify(mockDbIntegration, times(1)).getTemplate(any(String.class), any(String.class));
    }

    @Test
    void test_updateTemplate_whenTemplateDoesNotExist() {
        when(mockDbIntegration.getTemplate(any(String.class), any(String.class)))
            .thenReturn(Optional.empty());

        assertThatExceptionOfType(ThrowableProblem.class)
            .isThrownBy(() -> service.updateTemplate("someTemplateId", "3.0", mockJsonPatch));
    }

    @Test
    void test_deleteTemplate() {
        service.deleteTemplate("someTemplateId", null);

        verify(mockDbIntegration, times(1)).deleteTemplate(any(String.class), nullable(String.class));
    }
}
