package se.sundsvall.templating.api.domain;

import java.util.Map;

import se.sundsvall.templating.TemplateFlavor;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TemplateRequest {

    private String name;
    private String description;
    @Schema(example = "{\"HTML\": \"...content...\",\"TEXT\": \"...content...\"}")
    private Map<TemplateFlavor, String> variants;
}
