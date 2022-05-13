package se.sundsvall.templating.api.domain;

import javax.validation.constraints.Pattern;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TemplateRequest {

    @Pattern(regexp = "[A-Za-z0-9\\-\\.]+$")
    private String identifier;
    private String name;
    private String description;
    private String content;
}
