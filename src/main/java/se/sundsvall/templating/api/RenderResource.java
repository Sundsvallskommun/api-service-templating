package se.sundsvall.templating.api;

import javax.validation.Valid;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.zalando.problem.Problem;

import se.sundsvall.templating.api.domain.DirectRenderRequest;
import se.sundsvall.templating.api.domain.DirectRenderResponse;
import se.sundsvall.templating.api.domain.RenderRequest;
import se.sundsvall.templating.api.domain.RenderResponse;
import se.sundsvall.templating.service.TemplatingService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping(
    value = "/render",
    consumes = MediaType.APPLICATION_JSON_VALUE,
    produces = MediaType.APPLICATION_JSON_VALUE
)
@ApiResponses({
    @ApiResponse(
        responseCode = "500",
        description = "Internal Server Error",
        content = @Content(schema = @Schema(implementation = Problem.class))
    )
})
@Tag(name = "Rendering resources")
class RenderResource {

    private final TemplatingService templatingService;

    RenderResource(final TemplatingService templatingService) {
        this.templatingService = templatingService;
    }

    @Operation(summary = "Render a stored template, optionally with parameters")
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Successful operation",
            content = @Content(schema = @Schema(implementation = RenderResponse.class))
        )
    })
    @PostMapping
    ResponseEntity<RenderResponse> render(@Valid @RequestBody final RenderRequest request) {
        var response = RenderResponse.builder()
            .withOutput(templatingService.renderTemplate(request))
            .build();

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Render provided template contents, optionally with parameters")
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Successful operation",
            content = @Content(schema = @Schema(implementation = DirectRenderResponse.class))
        )
    })
    @PostMapping("/direct")
    ResponseEntity<DirectRenderResponse> renderDirect(@Valid @RequestBody final DirectRenderRequest request) {
        var response = DirectRenderResponse.builder()
            .withOutput(templatingService.renderDirect(request))
            .build();

        return ResponseEntity.ok(response);
    }
}
