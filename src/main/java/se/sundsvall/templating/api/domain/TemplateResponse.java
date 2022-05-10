package se.sundsvall.templating.api.domain;

import java.util.Map;

import se.sundsvall.templating.TemplateFlavor;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * A templates response.
 */
@Getter
@Builder(setterPrefix = "with")
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class TemplateResponse {

    private String id;
    private String identifier;
    private String name;
    private String description;

    @Schema(example = "{\"HTML\": \"...content...\",\"TEXT\": \"...content...\"}")
    private Map<TemplateFlavor, String> variants;
}
