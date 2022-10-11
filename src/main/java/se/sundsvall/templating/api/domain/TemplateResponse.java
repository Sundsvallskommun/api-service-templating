package se.sundsvall.templating.api.domain;

import java.time.LocalDateTime;
import java.util.List;

import io.swagger.v3.oas.annotations.media.ArraySchema;
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

    @Schema(description = "Version")
    private String version;

    @Schema(description = "Name")
    private String name;

    @Schema(description = "Description")
    private String description;

    @ArraySchema(schema = @Schema(description = "Metadata"))
    private List<Metadata> metadata;

    @ArraySchema(schema = @Schema(description = "Default values"))
    private List<DefaultValue> defaultValues;

    @Schema(description = "Changelog")
    private String changeLog;

    @Schema(description = "Last modification timestamp")
    private LocalDateTime lastModifiedAt;
}
