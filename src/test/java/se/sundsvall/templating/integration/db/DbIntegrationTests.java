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
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.data.jpa.domain.Specification;
import org.zalando.problem.ThrowableProblem;

import se.sundsvall.templating.domain.KeyValue;
import se.sundsvall.templating.integration.db.entity.TemplateEntity;
import se.sundsvall.templating.integration.db.entity.Version;

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
        when(mockTemplateRepository.findLatestByIdentifier(any(String.class)))
            .thenReturn(Optional.of(TemplateEntity.builder().build()));

        var optionalTemplate = dbIntegration.getTemplate("someTemplateId", null);
        assertThat(optionalTemplate).isPresent();

        verify(mockTemplateRepository, times(1)).findLatestByIdentifier(any(String.class));
    }


    @Test
    void test_getTemplateForProvidedVersion() {
        when(mockTemplateRepository.findByIdentifierAndVersion(any(String.class), any(Version.class)))
            .thenReturn(Optional.of(TemplateEntity.builder().build()));

        var optionalTemplate = dbIntegration.getTemplate("someTemplateId", "1.0");
        assertThat(optionalTemplate).isPresent();

        verify(mockTemplateRepository, times(1)).findByIdentifierAndVersion(any(String.class), any(Version.class));
    }

    @Test
    void test_findTemplate() {
        when(mockTemplateRepository.findOne(ArgumentMatchers.<Specification<TemplateEntity>>any()))
            .thenReturn(Optional.of(TemplateEntity.builder().build()));

        var optionalTemplate = dbIntegration.findTemplate(List.of(KeyValue.of("someKey", "someValue")));
        assertThat(optionalTemplate).isPresent();

        verify(mockTemplateRepository, times(1)).findOne(ArgumentMatchers.<Specification<TemplateEntity>>any());
    }

    @Test
    void test_findTemplate_exceptionThrownWhenMultipleResultsAreFound() {
        when(mockTemplateRepository.findOne(ArgumentMatchers.<Specification<TemplateEntity>>any()))
            .thenThrow(new IncorrectResultSizeDataAccessException(2));

        assertThatExceptionOfType(ThrowableProblem.class)
            .isThrownBy(() -> dbIntegration.findTemplate(List.of(KeyValue.of("someKey", "someValue"))));
    }

    @Test
    void test_findTemplates() {
        when(mockTemplateRepository.findAll(ArgumentMatchers.<Specification<TemplateEntity>>any()))
            .thenReturn(List.of(TemplateEntity.builder().build()));

        var result = dbIntegration.findTemplates(List.of(KeyValue.of("someKey", "someValue")));
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);

        verify(mockTemplateRepository, times(1)).findAll(ArgumentMatchers.<Specification<TemplateEntity>>any());
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

        dbIntegration.deleteTemplate("someTemplateId", null);

        verify(mockTemplateRepository, times(1)).existsByIdentifier(any(String.class));
        verify(mockTemplateRepository, times(1)).deleteByIdentifier(any(String.class));
    }


    @Test
    void test_deleteTemplateForProvidedVersion() {
        when(mockTemplateRepository.existsByIdentifierAndVersion(any(String.class), any(Version.class))).thenReturn(true);

        dbIntegration.deleteTemplate("someTemplateId", "1.0");

        verify(mockTemplateRepository, times(1)).existsByIdentifierAndVersion(any(String.class), any(Version.class));
        verify(mockTemplateRepository, times(1)).deleteByIdentifierAndVersion(any(String.class), any(Version.class));
    }

    @Test
    void test_deleteTemplate_whenTemplateDoesNotExist() {
        when(mockTemplateRepository.existsByIdentifier(any(String.class))).thenReturn(false);

        assertThatExceptionOfType(ThrowableProblem.class)
            .isThrownBy(() -> dbIntegration.deleteTemplate("someTemplateId", null));
    }


    @Test
    void test_deleteTemplate_whenTemplateDoesNotExistForProvidedVersion() {
        when(mockTemplateRepository.existsByIdentifierAndVersion(any(String.class), any(Version.class))).thenReturn(false);

        assertThatExceptionOfType(ThrowableProblem.class)
            .isThrownBy(() -> dbIntegration.deleteTemplate("someTemplateId", "1.0"));
    }

    @Test
    void test_toTemplateEntitySpecification() {
        var metadata = List.of(
            KeyValue.of("someKey", "someValue"), KeyValue.of("otherKey", "otherValue"));

        var result = dbIntegration.toTemplateEntitySpecification(metadata);
        assertThat(result).isNotNull();
    }


    @Test
    void test_toTemplateEntitySpecification_throwsExceptionWhenNoMetadataIsProvided() {
        assertThatExceptionOfType(IllegalStateException.class).isThrownBy(() -> dbIntegration.toTemplateEntitySpecification(List.of()));
    }
}
