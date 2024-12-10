package se.sundsvall.templating.configuration;

import io.pebbletemplates.pebble.PebbleEngine;
import io.pebbletemplates.pebble.lexer.Syntax;
import io.pebbletemplates.pebble.loader.StringLoader;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import se.sundsvall.templating.configuration.properties.PebbleProperties;
import se.sundsvall.templating.domain.ContextMunicipalityId;
import se.sundsvall.templating.integration.db.DbIntegration;
import se.sundsvall.templating.service.pebble.loader.DatabaseLoader;
import se.sundsvall.templating.service.pebble.loader.DelegatingLoader;

@Configuration
@EnableConfigurationProperties(PebbleProperties.class)
class PebbleConfiguration {

	private final PebbleProperties pebbleProperties;

	PebbleConfiguration(final PebbleProperties pebbleProperties) {
		this.pebbleProperties = pebbleProperties;
	}

	@Bean("pebble.database-loader")
	DatabaseLoader pebbleDatabaseLoader(final DbIntegration dbIntegration, final ContextMunicipalityId requestScropedMunicipalityId) {
		return new DatabaseLoader(dbIntegration, requestScropedMunicipalityId);
	}

	@Bean("pebble.string-loader")
	StringLoader pebbleStringLoader() {
		return new StringLoader();
	}

	@Bean("pebble.delegating-loader")
	DelegatingLoader pebbleDelegatingLoader(
		@Qualifier("pebble.database-loader") final DatabaseLoader databaseLoader,
		@Qualifier("pebble.string-loader") final StringLoader stringLoader,
		final ContextMunicipalityId requestScopedMunicipalityId) {
		return new DelegatingLoader(databaseLoader, stringLoader, requestScopedMunicipalityId);
	}

	@Bean
	PebbleEngine pebbleEngine(@Qualifier("pebble.delegating-loader") final DelegatingLoader loader) {
		return new PebbleEngine.Builder()
			.loader(loader)
			.syntax(syntax())
			.cacheActive(false)
			.autoEscaping(pebbleProperties.isAutoEscape())
			.build();
	}

	@Bean("debug-pebble-engine")
	PebbleEngine debugPebbleEngine(@Qualifier("pebble.delegating-loader") final DelegatingLoader loader) {
		return new PebbleEngine.Builder()
			.loader(loader)
			.syntax(syntax())
			.cacheActive(false)
			.autoEscaping(pebbleProperties.isAutoEscape())
			.strictVariables(true)
			.build();
	}

	Syntax syntax() {
		var syntaxBuilder = new Syntax.Builder()
			.setPrintOpenDelimiter(pebbleProperties.getDelimiters().getPrint().getOpen())
			.setPrintCloseDelimiter(pebbleProperties.getDelimiters().getPrint().getClose())
			.setExecuteOpenDelimiter(pebbleProperties.getDelimiters().getExecute().getOpen())
			.setExecuteCloseDelimiter(pebbleProperties.getDelimiters().getExecute().getClose())
			.setCommentOpenDelimiter(pebbleProperties.getDelimiters().getComment().getOpen())
			.setCommentCloseDelimiter(pebbleProperties.getDelimiters().getComment().getOpen());
		syntaxBuilder.setInterpolationOpenDelimiter(
			pebbleProperties.getDelimiters().getInterpolation().getOpen());
		syntaxBuilder.setInterpolationOpenDelimiter(
			pebbleProperties.getDelimiters().getInterpolation().getOpen());

		return syntaxBuilder.build();
	}
}
