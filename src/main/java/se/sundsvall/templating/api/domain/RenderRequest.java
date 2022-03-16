package se.sundsvall.templating.api.domain;

import java.util.Map;

import javax.validation.constraints.NotBlank;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * A request to render a template.
 */
@Getter
@Setter
public class RenderRequest {

    @NotBlank
    private String templateId;
    @Schema(example = "{ <JSON data> }")
    private Map<String, Object> parameters;
}
