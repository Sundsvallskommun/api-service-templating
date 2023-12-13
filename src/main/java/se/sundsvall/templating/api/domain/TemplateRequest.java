package se.sundsvall.templating.api.domain;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import java.util.List;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import se.sundsvall.templating.api.domain.validation.ValidTemplateId;
import se.sundsvall.templating.integration.db.entity.Version;

@Getter
@Setter
@Schema(description = "Template request")
public class TemplateRequest {

	@ValidTemplateId
	@Schema(description = "Identifier. May contain letters, digits, dashes and dots", requiredMode = REQUIRED)
	private String identifier;

	@Schema(description = "Version increment mode", enumAsRef = true)
	private Version.IncrementMode versionIncrement;

	@Schema(description = "Name", requiredMode = REQUIRED)
	private String name;

	@Schema(description = "Description", nullable = true)
	private String description;

	@Schema(description = "Content, as a BASE64-encoded string", requiredMode = REQUIRED)
	private String content;

	@ArraySchema(schema = @Schema(description = "Metadata"))
	private List<Metadata> metadata;

	@ArraySchema(schema = @Schema(description = "Default values"))
	private List<DefaultValue> defaultValues;

	@Schema(description = "A changelog")
	private String changeLog;
}
