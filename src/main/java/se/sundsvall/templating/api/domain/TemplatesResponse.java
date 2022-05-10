package se.sundsvall.templating.api.domain;

import java.util.List;

import se.sundsvall.templating.TemplateFlavor;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Templates response.
 */
@Getter
@Setter
@Builder(setterPrefix = "with")
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class TemplatesResponse {

    private String id;
    private String identifier;
    private String name;
    private String description;
    private List<TemplateFlavor> variants;
}
