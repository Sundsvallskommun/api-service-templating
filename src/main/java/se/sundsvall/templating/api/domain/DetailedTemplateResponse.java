package se.sundsvall.templating.api.domain;

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
@Schema(description = "Detailed template")
public class DetailedTemplateResponse extends TemplateResponse {

    @Schema(description = "Content, as a BASE64-encoded string")
    private String content;
}
