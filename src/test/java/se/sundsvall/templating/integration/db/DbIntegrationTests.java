package se.sundsvall.templating.integration.db;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
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

	@InjectMocks
	private DbIntegration dbIntegration;

	@Test
	void test_getAllTemplates() {
		when(mockTemplateRepository.findAllByMunicipalityId(any()))
			.thenReturn(List.of(TemplateEntity.builder().build(), TemplateEntity.builder().build()));

		var templates = dbIntegration.getAllTemplates(MUNICIPALITY_ID);
		assertThat(templates).hasSize(2);

		verify(mockTemplateRepository).findAllByMunicipalityId(MUNICIPALITY_ID);
	}

	@Test
	void test_getTemplate() {
		when(mockTemplateRepository.findLatestByIdentifierAndMunicipalityId(any(), any()))
			.thenReturn(Optional.of(TemplateEntity.builder().build()));

		var optionalTemplate = dbIntegration.getTemplate(MUNICIPALITY_ID, IDENTIFIER, null);
		assertThat(optionalTemplate).isPresent();

		verify(mockTemplateRepository).findLatestByIdentifierAndMunicipalityId(IDENTIFIER, MUNICIPALITY_ID);
	}

	@Test
	void test_getTemplateForProvidedVersion() {
		when(mockTemplateRepository.findByIdentifierAndVersionAndMunicipalityId(any(), any(), any()))
			.thenReturn(Optional.of(TemplateEntity.builder().build()));

		var optionalTemplate = dbIntegration.getTemplate(MUNICIPALITY_ID, IDENTIFIER, "1.0");
		assertThat(optionalTemplate).isPresent();

		verify(mockTemplateRepository).findByIdentifierAndVersionAndMunicipalityId(IDENTIFIER, new Version(1, 0), MUNICIPALITY_ID);
	}

	@Test
	void test_findTemplate() {
		when(mockTemplateRepository.findOne(ArgumentMatchers.<Specification<TemplateEntity>>any()))
			.thenReturn(Optional.of(TemplateEntity.builder().build()));

		var optionalTemplate = dbIntegration.findTemplate(MUNICIPALITY_ID, List.of(KeyValue.of("someKey", "someValue")));
		assertThat(optionalTemplate).isPresent();

		verify(mockTemplateRepository).findOne(ArgumentMatchers.<Specification<TemplateEntity>>any());
	}

	@Test
	void test_findTemplate_exceptionThrownWhenMultipleResultsAreFound() {
		when(mockTemplateRepository.findOne(ArgumentMatchers.<Specification<TemplateEntity>>any()))
			.thenThrow(new IncorrectResultSizeDataAccessException(2));

		var metadata = List.of(KeyValue.of("someKey", "someValue"));

		assertThatExceptionOfType(ThrowableProblem.class)
			.isThrownBy(() -> dbIntegration.findTemplate(MUNICIPALITY_ID, metadata));
	}

	@Test
	void test_findTemplates() {
		when(mockTemplateRepository.findAll(ArgumentMatchers.<Specification<TemplateEntity>>any()))
			.thenReturn(List.of(TemplateEntity.builder().build()));

		var result = dbIntegration.findTemplates(MUNICIPALITY_ID, List.of(KeyValue.of("someKey", "someValue")));
		assertThat(result)
			.isNotNull()
			.hasSize(1);

		verify(mockTemplateRepository).findAll(ArgumentMatchers.<Specification<TemplateEntity>>any());
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
		var templateEntity = TemplateEntity.builder().build();
		when(mockTemplateRepository.existsByIdentifierAndMunicipalityId(any(), any())).thenReturn(true);
		when(mockTemplateRepository.findByIdentifierAndMunicipalityId(any(), any())).thenReturn(List.of(templateEntity));

		dbIntegration.deleteTemplate(MUNICIPALITY_ID, IDENTIFIER, null);

		verify(mockTemplateRepository).existsByIdentifierAndMunicipalityId(IDENTIFIER, MUNICIPALITY_ID);
		verify(mockTemplateRepository).findByIdentifierAndMunicipalityId(IDENTIFIER, MUNICIPALITY_ID);
		verify(mockTemplateRepository).delete(same(templateEntity));
	}

	@Test
	void test_deleteTemplateForProvidedVersion() {
		var templateEntity = TemplateEntity.builder().build();
		when(mockTemplateRepository.findByIdentifierAndVersionAndMunicipalityId(any(), any(), any())).thenReturn(Optional.of(templateEntity));

		dbIntegration.deleteTemplate(MUNICIPALITY_ID, IDENTIFIER, "1.0");

		verify(mockTemplateRepository).findByIdentifierAndVersionAndMunicipalityId(IDENTIFIER, new Version(1, 0), MUNICIPALITY_ID);
		verify(mockTemplateRepository).delete(same(templateEntity));
	}

	@Test
	void test_deleteTemplate_whenTemplateDoesNotExist() {
		when(mockTemplateRepository.existsByIdentifierAndMunicipalityId(any(), any())).thenReturn(false);

		assertThatExceptionOfType(ThrowableProblem.class)
			.isThrownBy(() -> dbIntegration.deleteTemplate(MUNICIPALITY_ID, IDENTIFIER, null));
	}

	@Test
	void test_deleteTemplate_whenTemplateDoesNotExistForProvidedVersion() {
		when(mockTemplateRepository.findByIdentifierAndVersionAndMunicipalityId(any(), any(), any())).thenReturn(Optional.empty());

		assertThatExceptionOfType(ThrowableProblem.class)
			.isThrownBy(() -> dbIntegration.deleteTemplate(MUNICIPALITY_ID, IDENTIFIER, "1.0"))
			.withMessage("Not Found: Unable to find template 'someTemplateId:1.0'");

		verify(mockTemplateRepository).findByIdentifierAndVersionAndMunicipalityId(IDENTIFIER, Version.parse("1.0"), MUNICIPALITY_ID);
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
