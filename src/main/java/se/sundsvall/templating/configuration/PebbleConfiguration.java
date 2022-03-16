package se.sundsvall.templating.configuration;

import com.mitchellbosecke.pebble.PebbleEngine;
import com.mitchellbosecke.pebble.loader.StringLoader;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import se.sundsvall.templating.integration.db.DbIntegration;
import se.sundsvall.templating.service.pebble.DatabaseLoader;
import se.sundsvall.templating.service.pebble.DelegatingLoader;

@Configuration
class PebbleConfiguration {

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
            .build();
    }
}
