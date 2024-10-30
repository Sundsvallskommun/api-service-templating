package se.sundsvall.templating.integration.db;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.zalando.problem.Problem;
import org.zalando.problem.Status;

import se.sundsvall.templating.domain.KeyValue;
import se.sundsvall.templating.integration.db.entity.TemplateEntity;
import se.sundsvall.templating.integration.db.entity.TemplateEntity_;
import se.sundsvall.templating.integration.db.entity.Version;
import se.sundsvall.templating.integration.db.spec.Specifications;

@Component
@Transactional
public class DbIntegration {

	private final TemplateRepository templateRepository;

	public DbIntegration(final TemplateRepository templateRepository) {
		this.templateRepository = templateRepository;
	}

	@Transactional(readOnly = true)
	public List<TemplateEntity> getAllTemplates(final String municipalityId) {
		return templateRepository.findAllByMunicipalityId(municipalityId);
	}

	@Transactional(readOnly = true)
	public Optional<TemplateEntity> getTemplate(final String municipalityId, final String identifier, final String version) {
		return Optional.ofNullable(version)
			.map(Version::parse)
			.map(parsedVersion -> templateRepository.findByIdentifierAndVersionAndMunicipalityId(identifier, parsedVersion, municipalityId))
			.orElseGet(() -> templateRepository.findLatestByIdentifierAndMunicipalityId(identifier, municipalityId));
	}

	@Transactional(readOnly = true)
	public Optional<TemplateEntity> findTemplate(final String municipalityId, final List<KeyValue> metadata) {
		try {
			return templateRepository.findOne(toTemplateEntitySpecification(municipalityId, metadata));
		} catch (IncorrectResultSizeDataAccessException e) {
			throw Problem.valueOf(Status.NOT_FOUND, "Metadata query resulted in multiple matching templates");
		}
	}

	@Transactional(readOnly = true)
	public List<TemplateEntity> findTemplates(final String municipalityId, final Specification<TemplateEntity> filter) {
		var filterWithMunicipalityId = filter.and((root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get(TemplateEntity_.municipalityId), municipalityId));
		return templateRepository.findAll(filterWithMunicipalityId);
	}

	@Transactional(readOnly = true)
	public List<TemplateEntity> findTemplates(final String municipalityId, final List<KeyValue> metadata) {
		return templateRepository.findAll(toTemplateEntitySpecification(municipalityId, metadata));
	}

	Specification<TemplateEntity> toTemplateEntitySpecification(final String municipalityId, final List<KeyValue> metadata) {
		var specifications = new ArrayList<>(metadata.stream()
			.map(entry -> Specifications.hasMetadata(entry.getKey(), entry.getValue()))
			.toList());

		if (specifications.isEmpty()) {
			throw new IllegalStateException("No metadata specifications supplied");
		}

		var specification = specifications.remove(0);
		for (var additionalSpecification : specifications) {
			specification = specification.and(additionalSpecification);
		}

		specification = specification.and((root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get(TemplateEntity_.municipalityId), municipalityId));
		return specification;
	}

	public TemplateEntity saveTemplate(final TemplateEntity templateEntity) {
		return templateRepository.save(templateEntity);
	}

	public boolean existsByIdentifier(final String municipalityId, final String identifier) {
		return templateRepository.existsByIdentifierAndMunicipalityId(identifier, municipalityId);
	}

	public void deleteTemplate(final String municipalityId, final String identifier, final String version) {
		Optional.ofNullable(version)
			.map(Version::parse)
			.ifPresentOrElse(parsedVersion -> {
				if (!templateRepository.existsByIdentifierAndVersionAndMunicipalityId(identifier, parsedVersion, municipalityId)) {
					throw Problem.valueOf(Status.NOT_FOUND, "Unable to find template '" + identifier + ":" + parsedVersion + "'");
				}

				templateRepository.deleteByIdentifierAndVersionAndMunicipalityId(identifier, parsedVersion, municipalityId);
			}, () -> {
				if (!templateRepository.existsByIdentifierAndMunicipalityId(identifier, municipalityId)) {
					throw Problem.valueOf(Status.NOT_FOUND, "Unable to find template '" + identifier + "'");
				}

				templateRepository.deleteByIdentifierAndMunicipalityId(identifier, municipalityId);
			});
	}
}
