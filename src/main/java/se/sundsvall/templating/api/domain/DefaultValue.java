package se.sundsvall.templating.api.domain;

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
@Schema(description = "Template default value")
public class DefaultValue {

    @Schema(description = "Field name")
    private String fieldName;

    @Schema(description = "Value")
    private String value;
}
