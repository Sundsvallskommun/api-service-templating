package se.sundsvall.templating.api;

import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.zalando.problem.Problem;

import se.sundsvall.dept44.common.validators.annotation.ValidMunicipalityId;
import se.sundsvall.templating.api.domain.DirectRenderRequest;
import se.sundsvall.templating.api.domain.DirectRenderResponse;
import se.sundsvall.templating.api.domain.RenderRequest;
import se.sundsvall.templating.api.domain.RenderResponse;
import se.sundsvall.templating.domain.ContextMunicipalityId;
import se.sundsvall.templating.service.RenderingService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping(
	value = "/{municipalityId}/render",
	consumes = MediaType.APPLICATION_JSON_VALUE,
	produces = MediaType.APPLICATION_JSON_VALUE)
@ApiResponse(
	responseCode = "500",
	description = "Internal Server Error",
	content = @Content(schema = @Schema(implementation = Problem.class)))
@Tag(name = "Rendering resources")
class RenderResource {

	private final RenderingService renderingService;
	private final ContextMunicipalityId requestScopedMunicipalityId;

	RenderResource(final ContextMunicipalityId requestScopedMunicipalityId, final RenderingService renderingService) {
		this.requestScopedMunicipalityId = requestScopedMunicipalityId;
		this.renderingService = renderingService;
	}

	@Operation(
		summary = "Render a stored template, optionally with parameters",
		description = "Either 'identifier' or 'metadata' is required to identify the template to render")
	@ApiResponse(
		responseCode = "200",
		description = "Successful operation",
		content = @Content(schema = @Schema(implementation = RenderResponse.class)))
	@ApiResponse(
		responseCode = "404",
		description = "Not Found - the template could not be found",
		content = @Content(schema = @Schema(implementation = RenderResponse.class)))
	@PostMapping
	ResponseEntity<RenderResponse> render(
		@Parameter(name = "municipalityId", description = "Municipality ID", example = "2281") @ValidMunicipalityId @PathVariable final String municipalityId,
		@Valid @RequestBody final RenderRequest request) {
		requestScopedMunicipalityId.setId(municipalityId);
		final var output = renderingService.renderTemplate(municipalityId, request);

		final var response = RenderResponse.builder()
			.withOutput(output)
			.build();

		return ResponseEntity.ok(response);
	}

	@Operation(summary = "Render a stored template as a PDF, optionally with parameters")
	@ApiResponse(
		responseCode = "200",
		description = "Successful operation",
		content = @Content(schema = @Schema(implementation = RenderResponse.class)))
	@ApiResponse(
		responseCode = "404",
		description = "Not Found - the template could not be found",
		content = @Content(schema = @Schema(implementation = RenderResponse.class)))
	@PostMapping("/pdf")
	ResponseEntity<RenderResponse> renderPdf(
		@Parameter(name = "municipalityId", description = "Municipality ID", example = "2281") @ValidMunicipalityId @PathVariable final String municipalityId,
		@Valid @RequestBody final RenderRequest request) {
		requestScopedMunicipalityId.setId(municipalityId);
		final var output = renderingService.renderTemplateAsPdf(municipalityId, request);

		final var response = RenderResponse.builder()
			.withOutput(output)
			.build();

		return ResponseEntity.ok(response);
	}

	@Operation(summary = "Render provided template contents, optionally with parameters")
	@ApiResponse(
		responseCode = "200",
		description = "Successful operation",
		content = @Content(schema = @Schema(implementation = DirectRenderResponse.class)))
	@PostMapping("/direct")
	ResponseEntity<DirectRenderResponse> renderDirect(
		@Parameter(name = "municipalityId", description = "Municipality ID", example = "2281") @ValidMunicipalityId @PathVariable final String municipalityId,
		@Valid @RequestBody final DirectRenderRequest request) {
		requestScopedMunicipalityId.setId(municipalityId);
		final var response = DirectRenderResponse.builder()
			.withOutput(renderingService.renderDirect(request))
			.build();

		return ResponseEntity.ok(response);
	}

	@Operation(summary = "Render provided template contents as a PDF, optionally with parameters")
	@ApiResponse(
		responseCode = "200",
		description = "Successful operation",
		content = @Content(schema = @Schema(implementation = DirectRenderResponse.class)))
	@PostMapping("/direct/pdf")
	ResponseEntity<DirectRenderResponse> renderDirectPdf(
		@Parameter(name = "municipalityId", description = "Municipality ID", example = "2281") @ValidMunicipalityId @PathVariable final String municipalityId,
		@Valid @RequestBody final DirectRenderRequest request) {
		requestScopedMunicipalityId.setId(municipalityId);
		final var response = DirectRenderResponse.builder()
			.withOutput(renderingService.renderDirectAsPdf(request))
			.build();

		return ResponseEntity.ok(response);
	}
}
