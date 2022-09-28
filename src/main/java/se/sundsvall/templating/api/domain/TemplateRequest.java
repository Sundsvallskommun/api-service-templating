package se.sundsvall.templating.api.domain;

import java.util.List;

import se.sundsvall.templating.api.domain.validation.ValidTemplateId;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Template request")
public class TemplateRequest {

    @ValidTemplateId
    @Schema(description = "Identifier. May contain letters, digits, dashes and dots")
    private String identifier;

    @Schema(description = "Name")
    private String name;

    @Schema(description = "Description", nullable = true)
    private String description;

    @Schema(description = "Content, as a BASE64-encoded string")
    private String content;

    @ArraySchema(schema = @Schema(description = "Metadata"))
    private List<Metadata> metadata;

    @ArraySchema(schema = @Schema(description = "Default values"))
    private List<DefaultValue> defaultValues;
}
