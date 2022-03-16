package se.sundsvall.templating.api.domain;

import java.util.Map;

import javax.validation.constraints.NotBlank;

import lombok.Getter;
import lombok.Setter;

/**
 * A request to render a template directly.
 */
@Getter
@Setter
public class DirectRenderRequest {

    @NotBlank
    private String template;

    private Map<String, Object> parameters;
}
