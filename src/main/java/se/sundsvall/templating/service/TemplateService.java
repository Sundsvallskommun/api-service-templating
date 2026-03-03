package se.sundsvall.templating.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.JsonPatch;
import com.github.fge.jsonpatch.JsonPatchException;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import se.sundsvall.dept44.problem.Problem;
import se.sundsvall.templating.api.domain.DetailedTemplateResponse;
import se.sundsvall.templating.api.domain.TemplateRequest;
import se.sundsvall.templating.api.domain.TemplateResponse;
import se.sundsvall.templating.domain.KeyValue;
import se.sundsvall.templating.integration.db.DbIntegration;
import se.sundsvall.templating.integration.db.entity.TemplateContentEntity;
import se.sundsvall.templating.integration.db.entity.TemplateEntity;
import se.sundsvall.templating.integration.db.entity.Version;
import se.sundsvall.templating.service.mapper.TemplateMapper;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
public class TemplateService {

	private final ObjectMapper objectMapper;
	private final DbIntegration dbIntegration;
	private final TemplateMapper mapper;

	public TemplateService(final ObjectMapper objectMapper,
		final DbIntegration dbIntegration, final TemplateMapper mapper) {
		this.objectMapper = objectMapper;
		this.mapper = mapper;
		this.dbIntegration = dbIntegration;
	}

	@Transactional(readOnly = true)
	public List<TemplateResponse> getTemplates(final String municipalityId, final Specification<TemplateEntity> filter, final boolean showOnlyLatest) {
		return dbIntegration.findTemplates(municipalityId, filter, showOnlyLatest).stream().map(mapper::toTemplateResponse).toList();
	}

	@Transactional(readOnly = true)
	public List<TemplateResponse> getTemplates(final String municipalityId, final List<KeyValue> metadata, final boolean showOnlyLatest) {
		if (metadata == null || metadata.isEmpty()) {
			if (showOnlyLatest) {
				return dbIntegration.getAllLatestTemplates(municipalityId).stream()
					.map(mapper::toTemplateResponse)
					.toList();
			}
			return dbIntegration.getAllTemplates(municipalityId).stream()
				.map(mapper::toTemplateResponse)
				.toList();
		} else {
			return dbIntegration.findTemplates(municipalityId, metadata, showOnlyLatest).stream()
				.map(mapper::toTemplateResponse)
				.toList();
		}
	}

	@Transactional(readOnly = true)
	public Optional<DetailedTemplateResponse> getTemplate(final String municipalityId, final String identifier, final String version) {
		return dbIntegration.getTemplate(municipalityId, identifier, version)
			.map(mapper::toDetailedTemplateResponse);
	}

	public TemplateResponse saveTemplate(final String municipalityId, final TemplateRequest templateRequest) {
		final var version = dbIntegration.getTemplate(municipalityId, templateRequest.getIdentifier(), null)
			.map(templateEntity -> {
				if (null == templateRequest.getVersionIncrement()) {
					throw Problem.valueOf(BAD_REQUEST, "'versionIncrement' must be set, since template with identifier '" + templateRequest.getIdentifier() + "' already exists");
				}

				return templateEntity.getVersion().apply(templateRequest.getVersionIncrement());
			})
			.orElse(new Version(1, 0));

		final var templateEntity = mapper.toTemplateEntity(templateRequest, municipalityId)
			.withVersion(version)
			.withLatest(true);
		final var templateContentEntity = TemplateContentEntity.builder()
			.withId(templateEntity.getId())
			.withTemplate(templateEntity)
			.withContent(templateRequest.getContent())
			.build();
		templateEntity.setContent(templateContentEntity);

		return mapper.toTemplateResponse(dbIntegration.saveTemplate(templateEntity));
	}

	public TemplateResponse updateTemplate(final String municipalityId, final String identifier, final String version, final JsonPatch jsonPatch) {
		return dbIntegration.getTemplate(municipalityId, identifier, version)
			.map(templateEntity -> applyPatch(jsonPatch, TemplateEntity.class, templateEntity))
			.map(dbIntegration::saveTemplate)
			.map(mapper::toTemplateResponse)
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND, "Unable to find template '" + identifier + ":" + version + "'"));
	}

	public void deleteTemplate(final String municipalityId, final String identifier, final String version) {
		dbIntegration.deleteTemplate(municipalityId, identifier, version);
	}

	<T> T applyPatch(final JsonPatch patch, final Class<T> targetClass, final T target) {
		try {
			final var patched = patch.apply(objectMapper.convertValue(target, JsonNode.class));

			return objectMapper.treeToValue(patched, targetClass);
		} catch (final JsonPatchException | JsonProcessingException e) {
			throw new IllegalStateException("Unable to patch template entity", e);
		}
	}
}
