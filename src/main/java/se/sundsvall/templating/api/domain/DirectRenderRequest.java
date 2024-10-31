package se.sundsvall.templating.api.domain;

import java.util.Map;

import jakarta.validation.constraints.NotBlank;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Request to render a template directly")
public class DirectRenderRequest {

	@NotBlank
	@Schema(description = "The template content, as a BASE64-encoded string")
	private String content;

	@Schema(description = "Parameters (string values may be BASE64-encoded, and in that case they should be on the form \"BASE64:<base64-encoded-value>\")", nullable = true, example = OpenApiExamples.PARAMETERS)
	private Map<String, Object> parameters;
}
