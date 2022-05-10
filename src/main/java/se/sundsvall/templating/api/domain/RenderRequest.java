package se.sundsvall.templating.api.domain;

import java.util.Map;

import javax.validation.constraints.NotBlank;

import lombok.Getter;
import lombok.Setter;

/**
 * A request to render a template.
 */
@Getter
@Setter
public class RenderRequest {

    @NotBlank
    private String templateIdentifier;

    private Map<String, Object> parameters;
}
