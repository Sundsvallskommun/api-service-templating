package se.sundsvall.templating.configuration;

import com.mitchellbosecke.pebble.PebbleEngine;
import com.mitchellbosecke.pebble.lexer.Syntax;
import com.mitchellbosecke.pebble.loader.StringLoader;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import se.sundsvall.templating.configuration.properties.RenderingProperties;
import se.sundsvall.templating.configuration.properties.TemplateProperties;
import se.sundsvall.templating.integration.db.DbIntegration;
import se.sundsvall.templating.service.pebble.loader.DatabaseLoader;
import se.sundsvall.templating.service.pebble.loader.DelegatingLoader;

@Configuration
@EnableConfigurationProperties({TemplateProperties.class, RenderingProperties.class})
class TemplatingConfiguration {

    private final TemplateProperties templateProperties;

    TemplatingConfiguration(final TemplateProperties templateProperties) {
        this.templateProperties = templateProperties;
    }

    @Bean("pebble.database-loader")
    DatabaseLoader pebbleDatabaseLoader(final DbIntegration dbIntegration) {
        return new DatabaseLoader(dbIntegration);
    }

    @Bean("pebble.string-loader")
    StringLoader pebbleStringLoader() {
        return new StringLoader();
    }

    @Bean("pebble.delegating-loader")
    DelegatingLoader pebbleDelegatingLoader(
            @Qualifier("pebble.database-loader") final DatabaseLoader databaseLoader,
            @Qualifier("pebble.string-loader") final StringLoader stringLoader) {
        return new DelegatingLoader(databaseLoader, stringLoader);
    }

    @Bean
    PebbleEngine pebbleEngine(@Qualifier("pebble.delegating-loader") final DelegatingLoader loader) {
        return new PebbleEngine.Builder()
            .loader(loader)
            .syntax(syntax())
            .autoEscaping(templateProperties.isAutoEscape())
            .build();
    }

    @Bean("debug-pebble-engine")
    PebbleEngine debugPebbleEngine(@Qualifier("pebble.delegating-loader") final DelegatingLoader loader) {
        return new PebbleEngine.Builder()
            .loader(loader)
            .syntax(syntax())
            .autoEscaping(templateProperties.isAutoEscape())
            .strictVariables(true)
            .build();
    }

    Syntax syntax() {
        var syntaxBuilder = new Syntax.Builder()
            .setPrintOpenDelimiter(templateProperties.getDelimiters().getPrint().getOpen())
            .setPrintCloseDelimiter(templateProperties.getDelimiters().getPrint().getClose())
            .setExecuteOpenDelimiter(templateProperties.getDelimiters().getExecute().getOpen())
            .setExecuteCloseDelimiter(templateProperties.getDelimiters().getExecute().getClose())
            .setCommentOpenDelimiter(templateProperties.getDelimiters().getComment().getOpen())
            .setCommentCloseDelimiter(templateProperties.getDelimiters().getComment().getOpen());
        syntaxBuilder.setInterpolationOpenDelimiter(
            templateProperties.getDelimiters().getInterpolation().getOpen());
        syntaxBuilder.setInterpolationOpenDelimiter(
            templateProperties.getDelimiters().getInterpolation().getOpen());

        return syntaxBuilder.build();
    }
}
