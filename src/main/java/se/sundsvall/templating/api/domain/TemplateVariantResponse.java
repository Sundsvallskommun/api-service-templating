package se.sundsvall.templating.api.domain;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * A template variant response.
 */
@Getter
@Setter
@Builder(setterPrefix = "with")
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class TemplateVariantResponse {

    private String content;
}
