package se.sundsvall.templating.configuration.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ConfigurationProperties(prefix = "rendering")
public final class RenderingProperties {

    private boolean directOutputAsBase64 = false;
}
