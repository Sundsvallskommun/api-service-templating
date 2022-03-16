package se.sundsvall.templating.api.domain;

import java.util.Map;

import se.sundsvall.templating.TemplateFlavor;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder(setterPrefix = "with")
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class RenderResponse {

    @Schema(example = "{\"TEXT\": \"...\", \"HTML\": \"...\"}")
    private Map<TemplateFlavor, String> output;
}
