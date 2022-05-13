package se.sundsvall.templating.integration.db;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
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
import org.zalando.problem.ThrowableProblem;

import se.sundsvall.templating.integration.db.entity.TemplateEntity;

@ExtendWith(MockitoExtension.class)
class DbIntegrationTests {

    @Mock
    private TemplateRepository mockTemplateRepository;

    private DbIntegration dbIntegration;

    @BeforeEach
    void setUp() {
        dbIntegration = new DbIntegration(mockTemplateRepository);
    }

    @Test
    void test_getAllTemplates() {
        when(mockTemplateRepository.findAll())
            .thenReturn(List.of(TemplateEntity.builder().build(), TemplateEntity.builder().build()));

        var templates = dbIntegration.getAllTemplates();

        assertThat(templates).hasSize(2);

        verify(mockTemplateRepository, times(1)).findAll();
    }

    @Test
    void test_getTemplate() {
        when(mockTemplateRepository.findByIdentifier(any(String.class)))
            .thenReturn(Optional.of(TemplateEntity.builder().build()));

        var optionalTemplate = dbIntegration.getTemplate("someTemplateId");

        assertThat(optionalTemplate).isPresent();

        verify(mockTemplateRepository, times(1)).findByIdentifier(any(String.class));
    }

    @Test
    void test_saveTemplate() {
        when(mockTemplateRepository.save(any(TemplateEntity.class)))
            .thenReturn(TemplateEntity.builder().build());

        var templateEntity = dbIntegration.saveTemplate(TemplateEntity.builder().build());

        assertThat(templateEntity).isNotNull();

        verify(mockTemplateRepository, times(1)).save(any(TemplateEntity.class));
    }

    @Test
    void test_deleteTemplate() {
        when(mockTemplateRepository.existsByIdentifier(any(String.class))).thenReturn(true);

        dbIntegration.deleteTemplate("someTemplateId");

        verify(mockTemplateRepository, times(1)).existsByIdentifier(any(String.class));
        verify(mockTemplateRepository, times(1)).deleteByIdentifier(any(String.class));
    }

    @Test
    void test_deleteTemplate_whenTemplateDoesNotExist() {
        when(mockTemplateRepository.existsByIdentifier(any(String.class))).thenReturn(false);

        assertThatExceptionOfType(ThrowableProblem.class)
            .isThrownBy(() -> dbIntegration.deleteTemplate("someTemplateId"));
    }
}
