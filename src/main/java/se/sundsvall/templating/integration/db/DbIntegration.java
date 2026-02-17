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
	public List<TemplateEntity> getAllLatestTemplates(final String municipalityId) {
		return templateRepository.findAllByMunicipalityIdAndLatestTrue(municipalityId);
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
		} catch (final IncorrectResultSizeDataAccessException _) {
			throw Problem.valueOf(Status.NOT_FOUND, "Metadata query resulted in multiple matching templates");
		}
	}

	@Transactional(readOnly = true)
	public List<TemplateEntity> findTemplates(final String municipalityId, final Specification<TemplateEntity> filter, final boolean showOnlyLatest) {
		var filterWithMunicipalityId = filter.and((root, _, criteriaBuilder) -> criteriaBuilder.equal(root.get(TemplateEntity_.municipalityId), municipalityId));
		if (showOnlyLatest) {
			filterWithMunicipalityId = filterWithMunicipalityId.and(Specifications.isLatest());
		}
		return templateRepository.findAll(filterWithMunicipalityId);
	}

	@Transactional(readOnly = true)
	public List<TemplateEntity> findTemplates(final String municipalityId, final List<KeyValue> metadata, final boolean showOnlyLatest) {
		var specification = toTemplateEntitySpecification(municipalityId, metadata);
		if (showOnlyLatest) {
			specification = specification.and(Specifications.isLatest());
		}
		return templateRepository.findAll(specification);
	}

	Specification<TemplateEntity> toTemplateEntitySpecification(final String municipalityId, final List<KeyValue> metadata) {
		final var specifications = new ArrayList<>(metadata.stream()
			.map(entry -> Specifications.hasMetadata(entry.getKey(), entry.getValue()))
			.toList());

		if (specifications.isEmpty()) {
			throw new IllegalStateException("No metadata specifications supplied");
		}

		var specification = specifications.removeFirst();
		for (final var additionalSpecification : specifications) {
			specification = specification.and(additionalSpecification);
		}

		specification = specification.and((root, _, criteriaBuilder) -> criteriaBuilder.equal(root.get(TemplateEntity_.municipalityId), municipalityId));
		return specification;
	}

	public TemplateEntity saveTemplate(final TemplateEntity templateEntity) {
		if (templateEntity.isLatest()) {
			templateRepository.findByIdentifierAndMunicipalityIdAndLatestTrue(templateEntity.getIdentifier(), templateEntity.getMunicipalityId())
				.filter(existing -> !existing.getId().equals(templateEntity.getId()))
				.ifPresent(existing -> {
					existing.setLatest(false);
					templateRepository.save(existing);
				});
		}
		return templateRepository.save(templateEntity);
	}

	public void deleteTemplate(final String municipalityId, final String identifier, final String version) {
		Optional.ofNullable(version)
			.map(Version::parse)
			.ifPresentOrElse(parsedVersion -> {
				final var templateEntity = templateRepository.findByIdentifierAndVersionAndMunicipalityId(identifier, parsedVersion, municipalityId)
					.orElseThrow(() -> Problem.valueOf(Status.NOT_FOUND, "Unable to find template '" + identifier + ":" + parsedVersion + "'"));
				final var wasLatest = templateEntity.isLatest();
				templateRepository.delete(templateEntity);
				if (wasLatest) {
					templateRepository.flush();
					templateRepository.findLatestByIdentifierAndMunicipalityId(identifier, municipalityId)
						.ifPresent(next -> {
							next.setLatest(true);
							templateRepository.save(next);
						});
				}
			}, () -> {
				if (!templateRepository.existsByIdentifierAndMunicipalityId(identifier, municipalityId)) {
					throw Problem.valueOf(Status.NOT_FOUND, "Unable to find template '" + identifier + "'");
				}
				templateRepository.deleteAll(templateRepository.findByIdentifierAndMunicipalityId(identifier, municipalityId));
			});
	}
}
