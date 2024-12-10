package se.sundsvall.templating.integration.db;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
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

	public static final String MUNICIPALITY_ID = "municipalityId";
	public static final String IDENTIFIER = "someTemplateId";

	@Mock
	private TemplateRepository mockTemplateRepository;

	private DbIntegration dbIntegration;

	@BeforeEach
	void setUp() {
		dbIntegration = new DbIntegration(mockTemplateRepository);
	}

	@Test
	void test_getAllTemplates() {
		when(mockTemplateRepository.findAllByMunicipalityId(any()))
			.thenReturn(List.of(TemplateEntity.builder().build(), TemplateEntity.builder().build()));

		var templates = dbIntegration.getAllTemplates(MUNICIPALITY_ID);
		assertThat(templates).hasSize(2);

		verify(mockTemplateRepository, times(1)).findAllByMunicipalityId(MUNICIPALITY_ID);
	}

	@Test
	void test_getTemplate() {
		when(mockTemplateRepository.findLatestByIdentifierAndMunicipalityId(any(), any()))
			.thenReturn(Optional.of(TemplateEntity.builder().build()));

		var optionalTemplate = dbIntegration.getTemplate(MUNICIPALITY_ID, IDENTIFIER, null);
		assertThat(optionalTemplate).isPresent();

		verify(mockTemplateRepository, times(1)).findLatestByIdentifierAndMunicipalityId(IDENTIFIER, MUNICIPALITY_ID);
	}

	@Test
	void test_getTemplateForProvidedVersion() {
		when(mockTemplateRepository.findByIdentifierAndVersionAndMunicipalityId(any(), any(), any()))
			.thenReturn(Optional.of(TemplateEntity.builder().build()));

		var optionalTemplate = dbIntegration.getTemplate(MUNICIPALITY_ID, IDENTIFIER, "1.0");
		assertThat(optionalTemplate).isPresent();

		verify(mockTemplateRepository, times(1)).findByIdentifierAndVersionAndMunicipalityId(IDENTIFIER, new Version(1, 0), MUNICIPALITY_ID);
	}

	@Test
	void test_findTemplate() {
		when(mockTemplateRepository.findOne(ArgumentMatchers.<Specification<TemplateEntity>>any()))
			.thenReturn(Optional.of(TemplateEntity.builder().build()));

		var optionalTemplate = dbIntegration.findTemplate(MUNICIPALITY_ID, List.of(KeyValue.of("someKey", "someValue")));
		assertThat(optionalTemplate).isPresent();

		verify(mockTemplateRepository, times(1)).findOne(ArgumentMatchers.<Specification<TemplateEntity>>any());
	}

	@Test
	void test_findTemplate_exceptionThrownWhenMultipleResultsAreFound() {
		when(mockTemplateRepository.findOne(ArgumentMatchers.<Specification<TemplateEntity>>any()))
			.thenThrow(new IncorrectResultSizeDataAccessException(2));

		assertThatExceptionOfType(ThrowableProblem.class)
			.isThrownBy(() -> dbIntegration.findTemplate(MUNICIPALITY_ID, List.of(KeyValue.of("someKey", "someValue"))));
	}

	@Test
	void test_findTemplates() {
		when(mockTemplateRepository.findAll(ArgumentMatchers.<Specification<TemplateEntity>>any()))
			.thenReturn(List.of(TemplateEntity.builder().build()));

		var result = dbIntegration.findTemplates(MUNICIPALITY_ID, List.of(KeyValue.of("someKey", "someValue")));
		assertThat(result)
			.isNotNull()
			.hasSize(1);

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
		when(mockTemplateRepository.existsByIdentifierAndMunicipalityId(any(), any())).thenReturn(true);

		dbIntegration.deleteTemplate(MUNICIPALITY_ID, IDENTIFIER, null);

		verify(mockTemplateRepository, times(1)).existsByIdentifierAndMunicipalityId(IDENTIFIER, MUNICIPALITY_ID);
		verify(mockTemplateRepository, times(1)).deleteByIdentifierAndMunicipalityId(IDENTIFIER, MUNICIPALITY_ID);
	}

	@Test
	void test_deleteTemplateForProvidedVersion() {
		when(mockTemplateRepository.existsByIdentifierAndVersionAndMunicipalityId(any(), any(), any())).thenReturn(true);

		dbIntegration.deleteTemplate(MUNICIPALITY_ID, IDENTIFIER, "1.0");

		verify(mockTemplateRepository, times(1)).existsByIdentifierAndVersionAndMunicipalityId(IDENTIFIER, new Version(1, 0), MUNICIPALITY_ID);
		verify(mockTemplateRepository, times(1)).deleteByIdentifierAndVersionAndMunicipalityId(IDENTIFIER, new Version(1, 0), MUNICIPALITY_ID);
	}

	@Test
	void test_deleteTemplate_whenTemplateDoesNotExist() {
		when(mockTemplateRepository.existsByIdentifierAndMunicipalityId(any(), any())).thenReturn(false);

		assertThatExceptionOfType(ThrowableProblem.class)
			.isThrownBy(() -> dbIntegration.deleteTemplate(MUNICIPALITY_ID, IDENTIFIER, null));
	}

	@Test
	void test_deleteTemplate_whenTemplateDoesNotExistForProvidedVersion() {
		when(mockTemplateRepository.existsByIdentifierAndVersionAndMunicipalityId(any(), any(), any())).thenReturn(false);

		assertThatExceptionOfType(ThrowableProblem.class)
			.isThrownBy(() -> dbIntegration.deleteTemplate(MUNICIPALITY_ID, IDENTIFIER, "1.0"));
	}

	@Test
	void test_toTemplateEntitySpecification() {
		var metadata = List.of(
			KeyValue.of("someKey", "someValue"), KeyValue.of("otherKey", "otherValue"));

		var result = dbIntegration.toTemplateEntitySpecification(MUNICIPALITY_ID, metadata);
		assertThat(result).isNotNull();
	}

	@Test
	void test_toTemplateEntitySpecification_throwsExceptionWhenNoMetadataIsProvided() {
		var metadata = new ArrayList<KeyValue>();

		assertThatExceptionOfType(IllegalStateException.class)
			.isThrownBy(() -> dbIntegration.toTemplateEntitySpecification(MUNICIPALITY_ID, metadata));
	}
}
