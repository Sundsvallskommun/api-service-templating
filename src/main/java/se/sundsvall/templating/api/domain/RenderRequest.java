package se.sundsvall.templating.api.domain;

import java.util.List;
import java.util.Map;

import javax.validation.Valid;

import se.sundsvall.templating.api.domain.validation.ValidRenderRequest;
import se.sundsvall.templating.domain.KeyValue;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder(setterPrefix = "with")
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@ValidRenderRequest
@Schema(description = "Request to render a template")
public class RenderRequest {

    @Schema(description = "Template identifier", nullable = true)
    private String identifier;

    @Schema(description = "Template metadata", nullable = true)
    private List<@Valid KeyValue> metadata;

    @Schema(description = "Parameters", nullable = true)
    private Map<String, Object> parameters;
}
