package se.sundsvall.templating.api.domain;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import se.sundsvall.templating.api.domain.validation.ValidRenderRequest;
import se.sundsvall.templating.api.domain.validation.ValidTemplateVersion;
import se.sundsvall.templating.domain.KeyValue;

@Getter
@Setter
@Builder(setterPrefix = "with")
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@ValidRenderRequest
@Schema(description = "Request to render a template")
public class RenderRequest {

	@Schema(description = "Template identifier", types = {
		"string", "null"
	})
	private String identifier;

	@ValidTemplateVersion
	@Schema(description = "Template version", types = {
		"string", "null"
	})
	private String version;

	@ArraySchema(schema = @Schema(description = "Template metadata"))
	private List<@Valid KeyValue> metadata;

	@Schema(description = "Parameters (string values may be BASE64-encoded, and in that case they should be on the form \"BASE64:<base64-encoded-value>\")", types = {
		"string", "null"
	}, examples = OpenApiExamples.PARAMETERS)
	private Map<String, Object> parameters;
}
