package se.sundsvall.templating.api;

import java.util.List;

import javax.validation.Valid;

import com.github.fge.jsonpatch.JsonPatch;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;
import org.zalando.problem.Problem;

import se.sundsvall.templating.TemplateFlavor;
import se.sundsvall.templating.api.domain.TemplateRequest;
import se.sundsvall.templating.api.domain.TemplateResponse;
import se.sundsvall.templating.api.domain.TemplateVariantResponse;
import se.sundsvall.templating.api.domain.TemplatesResponse;
import se.sundsvall.templating.service.TemplatingService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping(value = "/template", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Template resources")
@ApiResponses({
    @ApiResponse(
        responseCode = "500",
        description = "Internal Server Error",
        content = @Content(schema = @Schema(implementation = Problem.class))
    )
})
class TemplateResource {

    private final TemplatingService templatingService;

    TemplateResource(final TemplatingService templatingService) {
        this.templatingService = templatingService;
    }

    @Operation(summary = "Get all available templates")
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Successful operation",
            content = @Content(
                array = @ArraySchema(schema = @Schema(implementation = TemplatesResponse.class))
            )
        )
    })
    @GetMapping
    List<TemplatesResponse> getAllTemplates() {
        return templatingService.getAllTemplates().stream()
            .map(templateResponse -> TemplatesResponse.builder()
                .withId(templateResponse.getId())
                .withName(templateResponse.getName())
                .withDescription(templateResponse.getDescription())
                .withVariants(List.copyOf(templateResponse.getVariants().keySet()))
                .build())
            .toList();
    }

    @Operation(summary = "Get a template by id, including all variants")
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Successful operation",
            content = @Content(schema = @Schema(implementation = TemplateResponse.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Not Found",
            content = @Content(schema = @Schema(implementation = Problem.class))
        )
    })
    @GetMapping("/{id}")
    ResponseEntity<TemplateResponse> getTemplate(@PathVariable("id") final String id) {
        return templatingService.getTemplate(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Get a template variant")
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Successful operation",
            content = @Content(schema = @Schema(implementation = TemplateVariantResponse.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Not Found",
            content = @Content(schema = @Schema(implementation = Problem.class))
        )
    })
    @GetMapping("/{id}/{flavor}")
    ResponseEntity<TemplateVariantResponse> getTemplateVariant(@PathVariable("id") final String id,
            @PathVariable("flavor") final TemplateFlavor flavor) {
        return templatingService.getTemplate(id)
            .map(templateResponse -> templateResponse.getVariants().get(flavor))
            .map(templateVariantContent -> TemplateVariantResponse.builder()
                .withContent(templateVariantContent)
                .build())
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Store a template")
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Successful operation",
            content = @Content(schema = @Schema(implementation = TemplateResponse.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Bad Request",
            content = @Content(schema = @Schema(implementation = Problem.class))
        )
    })
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<TemplateResponse> saveTemplate(@Valid @RequestBody final TemplateRequest request) {
        var template = templatingService.saveTemplate(request);

        var uri = UriComponentsBuilder.fromPath("/template")
            .pathSegment(template.getId())
            .build()
            .toUri();

        return ResponseEntity.created(uri).body(template);
    }

    @Operation(summary = "Update a template")
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Successful operation",
            content = @Content(schema = @Schema(implementation = TemplateResponse.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Bad Request",
            content = @Content(schema = @Schema(implementation = Problem.class))
        )
    })
    @PatchMapping(value = "/{id}", consumes = "application/json-patch+json")
    ResponseEntity<TemplateResponse> updateTemplate(@PathVariable("id") final String id,
            @RequestBody
            @Schema(example = "[{\"op\":\"add|remove|replace\",\"path\": \"/some/attribute/path\",\"value\": \"...\"}]")
            final JsonPatch jsonPatch) {
        var template = templatingService.updateTemplate(id, jsonPatch);

        return ResponseEntity.ok(template);
    }

    @Operation(summary = "Delete a template")
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Successful operation",
            content = @Content(schema = @Schema(implementation = TemplateResponse.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Not Found",
            content = @Content(schema = @Schema(implementation = Problem.class))
        )
    })
    @DeleteMapping("/{id}")
    ResponseEntity<Void> deleteTemplate(@PathVariable("id") final String id) {
        templatingService.deleteTemplate(id);

        return ResponseEntity.ok().build();
    }
}
