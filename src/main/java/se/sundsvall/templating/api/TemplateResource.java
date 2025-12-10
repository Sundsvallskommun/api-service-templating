package se.sundsvall.templating.api;

import com.github.fge.jsonpatch.JsonPatch;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;
import org.zalando.problem.Problem;
import se.sundsvall.dept44.common.validators.annotation.ValidMunicipalityId;
import se.sundsvall.templating.api.domain.DetailedTemplateResponse;
import se.sundsvall.templating.api.domain.OpenApiExamples;
import se.sundsvall.templating.api.domain.TemplateRequest;
import se.sundsvall.templating.api.domain.TemplateResponse;
import se.sundsvall.templating.api.domain.filter.MetadataFilterSpecifications;
import se.sundsvall.templating.api.domain.filter.expression.Expression;
import se.sundsvall.templating.api.domain.validation.ValidTemplateId;
import se.sundsvall.templating.api.domain.validation.ValidTemplateVersion;
import se.sundsvall.templating.domain.KeyValue;
import se.sundsvall.templating.service.TemplateService;

@Validated
@RestController
@RequestMapping(value = "/{municipalityId}/templates", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Template resources")
@ApiResponse(
	responseCode = "500",
	description = "Internal Server Error",
	content = @Content(schema = @Schema(implementation = Problem.class)))
class TemplateResource {

	private final TemplateService templatingService;

	TemplateResource(final TemplateService templatingService) {
		this.templatingService = templatingService;
	}

	@Operation(summary = "Search available templates by metadata, content excluded")
	@ApiResponse(
		responseCode = "200",
		description = "Successful operation",
		content = @Content(
			array = @ArraySchema(schema = @Schema(implementation = TemplateResponse.class))))
	@PostMapping("/search")
	List<TemplateResponse> searchTemplates(
		@Parameter(name = "municipalityId", description = "Municipality ID", example = "2281") @ValidMunicipalityId @PathVariable final String municipalityId,
		@RequestBody final Expression expression) {
		return templatingService.getTemplates(municipalityId, MetadataFilterSpecifications.toSpecification(expression));
	}

	@Operation(summary = "Get all available templates, content excluded")
	@ApiResponse(
		responseCode = "200",
		description = "Successful operation",
		content = @Content(
			array = @ArraySchema(schema = @Schema(implementation = TemplateResponse.class))))
	@GetMapping
	List<TemplateResponse> getAllTemplates(
		@Parameter(name = "municipalityId", description = "Municipality ID", example = "2281") @ValidMunicipalityId @PathVariable final String municipalityId,
		@RequestParam(defaultValue = "{}") @Parameter(description = "Metadata filters (dictionary/map: <code>{ \"key\": \"value\", ... }</code> ). Not required") final Map<String, String> filters) {
		final var metadata = filters.entrySet().stream()
			.map(filter -> KeyValue.of(filter.getKey(), filter.getValue()))
			.toList();

		return templatingService.getTemplates(municipalityId, metadata);
	}

	@Operation(summary = "Get the latest version of a template by identifier, including content")
	@ApiResponse(
		responseCode = "200",
		description = "Successful operation",
		content = @Content(schema = @Schema(implementation = DetailedTemplateResponse.class)))
	@ApiResponse(
		responseCode = "404",
		description = "Not Found",
		content = @Content(schema = @Schema(implementation = Problem.class)))
	@GetMapping("/{identifier}")
	ResponseEntity<DetailedTemplateResponse> getTemplate(
		@Parameter(name = "municipalityId", description = "Municipality ID", example = "2281") @ValidMunicipalityId @PathVariable final String municipalityId,
		@PathVariable("identifier") @ValidTemplateId final String identifier) {
		return getTemplate(municipalityId, identifier, null);
	}

	@Operation(summary = "Get a specific version of a template by identifier, including content")
	@ApiResponse(
		responseCode = "200",
		description = "Successful operation",
		content = @Content(schema = @Schema(implementation = DetailedTemplateResponse.class)))
	@ApiResponse(
		responseCode = "404",
		description = "Not Found",
		content = @Content(schema = @Schema(implementation = Problem.class)))
	@GetMapping("/{identifier}/{version}")
	ResponseEntity<DetailedTemplateResponse> getTemplate(
		@Parameter(name = "municipalityId", description = "Municipality ID", example = "2281") @ValidMunicipalityId @PathVariable final String municipalityId,
		@PathVariable("identifier") @ValidTemplateId final String identifier,
		@PathVariable("version") @ValidTemplateVersion final String version) {
		return templatingService.getTemplate(municipalityId, identifier, version)
			.map(ResponseEntity::ok)
			.orElse(ResponseEntity.notFound().build());
	}

	@Operation(summary = "Store a template")
	@ApiResponse(
		responseCode = "200",
		description = "Successful operation",
		content = @Content(schema = @Schema(implementation = TemplateResponse.class)))
	@ApiResponse(
		responseCode = "400",
		description = "Bad Request",
		content = @Content(schema = @Schema(implementation = Problem.class)))
	@PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
	ResponseEntity<TemplateResponse> saveTemplate(
		@Parameter(name = "municipalityId", description = "Municipality ID", example = "2281") @ValidMunicipalityId @PathVariable final String municipalityId,
		@Valid @RequestBody final TemplateRequest request) {
		final var template = templatingService.saveTemplate(municipalityId, request);

		final var uri = UriComponentsBuilder.fromPath("/template")
			.pathSegment(template.getIdentifier())
			.build()
			.toUri();

		return ResponseEntity.created(uri).body(template);
	}

	@Operation(summary = "Update (specific version of) a template")
	@ApiResponse(
		responseCode = "200",
		description = "Successful operation",
		content = @Content(schema = @Schema(implementation = TemplateResponse.class)))
	@ApiResponse(
		responseCode = "400",
		description = "Bad Request",
		content = @Content(schema = @Schema(implementation = Problem.class)))
	@ApiResponse(
		responseCode = "404",
		description = "Not Found",
		content = @Content(schema = @Schema(implementation = Problem.class)))
	@PatchMapping(value = "/{identifier}/{version}", consumes = "application/json-patch+json")
	ResponseEntity<TemplateResponse> updateTemplate(
		@Parameter(name = "municipalityId", description = "Municipality ID", example = "2281") @ValidMunicipalityId @PathVariable final String municipalityId,
		@PathVariable("identifier") @ValidTemplateId final String identifier,
		@PathVariable("version") @ValidTemplateVersion final String version,
		@RequestBody @Schema(examples = OpenApiExamples.UPDATE) final JsonPatch jsonPatch) {
		final var template = templatingService.updateTemplate(municipalityId, identifier, version, jsonPatch);

		return ResponseEntity.ok(template);
	}

	@Operation(summary = "Delete a template, including all its versions")
	@ApiResponse(
		responseCode = "200",
		description = "Successful operation",
		content = @Content(schema = @Schema(implementation = TemplateResponse.class)))
	@ApiResponse(
		responseCode = "404",
		description = "Not Found",
		content = @Content(schema = @Schema(implementation = Problem.class)))
	@DeleteMapping("/{identifier}")
	ResponseEntity<Void> deleteTemplate(
		@Parameter(name = "municipalityId", description = "Municipality ID", example = "2281") @ValidMunicipalityId @PathVariable final String municipalityId,
		@PathVariable("identifier") @ValidTemplateId final String identifier) {
		templatingService.deleteTemplate(municipalityId, identifier, null);

		return ResponseEntity.ok().build();
	}

	@Operation(summary = "Delete a specific version of a template")
	@ApiResponse(
		responseCode = "200",
		description = "Successful operation",
		content = @Content(schema = @Schema(implementation = TemplateResponse.class)))
	@ApiResponse(
		responseCode = "404",
		description = "Not Found",
		content = @Content(schema = @Schema(implementation = Problem.class)))
	@DeleteMapping("/{identifier}/{version}")
	ResponseEntity<Void> deleteTemplate(
		@Parameter(name = "municipalityId", description = "Municipality ID", example = "2281") @ValidMunicipalityId @PathVariable final String municipalityId,
		@PathVariable("identifier") @ValidTemplateId final String identifier,
		@PathVariable("version") @ValidTemplateVersion final String version) {
		templatingService.deleteTemplate(municipalityId, identifier, version);

		return ResponseEntity.ok().build();
	}
}
