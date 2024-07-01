package se.sundsvall.templating.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.annotation.RequestScope;
import se.sundsvall.templating.domain.ContextMunicipalityId;

@Configuration
public class MunicipalityIdConfiguration {

	@Bean
	@RequestScope
	ContextMunicipalityId requestScopedBean() {
		return new ContextMunicipalityId();
	}
}
