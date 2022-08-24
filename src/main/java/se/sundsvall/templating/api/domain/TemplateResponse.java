package se.sundsvall.templating.api.domain;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder(setterPrefix = "with")
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Schema(description = "Template")
public class TemplateResponse {

    @Schema(description = "Identifier")
    private String identifier;

    @Schema(description = "Name")
    private String name;

    @Schema(description = "Description")
    private String description;

    @Schema(description = "Metadata")
    private List<Metadata> metadata;

    @Schema(description = "Default values")
    private List<DefaultValue> defaultValues;
}
