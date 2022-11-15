package se.sundsvall.templating;

import static se.sundsvall.dept44.util.ResourceUtils.asString;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.Resource;

import se.sundsvall.templating.integration.db.DbIntegration;
import se.sundsvall.templating.integration.db.TemplateRepository;
import se.sundsvall.templating.integration.db.entity.DefaultValueEntity;
import se.sundsvall.templating.integration.db.entity.MetadataEntity;
import se.sundsvall.templating.integration.db.entity.TemplateEntity;
import se.sundsvall.templating.integration.db.entity.Version;
import se.sundsvall.templating.util.BASE64;

/*
 * To be removed - atm only exists for simplifying local development, populating initial data and stuff...
 */
@Configuration
class LocalDataLoader {

    @Bean
    @Profile("default")
    CommandLineRunner testStuff1(final TemplateRepository templateRepository,
            final DbIntegration dbIntegration,
            @Value("classpath:/templates/logo.peb") final Resource logoTemplateResource,
            @Value("classpath:/templates/avslag.peb") final Resource avslagTemplateResource,
            @Value("classpath:/templates/bifall.peb") final Resource bifallTemplateResource) {
        return args -> {
            templateRepository.deleteAll();

            var logo = TemplateEntity.builder()
                .withIdentifier("common.resources.sundsvalls-kommun-logo")
                .withName("Sundsvalls Kommun Logo")
                .withContent(BASE64.encode(asString(logoTemplateResource, StandardCharsets.UTF_8)))
                .build();
            dbIntegration.saveTemplate(logo);

            var bifallTemplate = TemplateEntity.builder()
                .withIdentifier("example.bifall")
                .withName("P-tillstånd Bifall")
                .withContent(BASE64.encode(asString(bifallTemplateResource, StandardCharsets.UTF_8)))
                .build();
            dbIntegration.saveTemplate(bifallTemplate);

/*
            var avslagTemplate = TemplateEntity.builder()
                .withIdentifier("example.avslag")
                .withName("P-tillstånd Avslag")
                .withContent(asString(avslagTemplateResource, StandardCharsets.UTF_8))
                .build();
            dbIntegration.saveTemplate(avslagTemplate);

            var test = TemplateEntity.builder()
                .withIdentifier("test.default-values")
                .withName("Default values test template")
                .withContent("This is a template with a [[ DEFAULT_NAME ]]")
                .withDefaultValues(Set.of(
                    DefaultValueEntity.builder()
                        .withFieldName("DEFAULT_NAME")
                        .withValue("[DEFAULT VALUE]")
                        .build()
                ))
                .build();
            dbIntegration.saveTemplate(test);
*/

            var tmpl = TemplateEntity.builder()
                .withIdentifier("some.random.identifier")
                .withName("Some template")
                .withDescription("Template description goes here...")
                .withContent(BASE64.encode("someContent..."))
                .withMetadata(List.of(
                    MetadataEntity.builder()
                        .withKey("verksamhet")
                        .withValue("SBK")
                        .build(),
                    MetadataEntity.builder()
                        .withKey("process")
                        .withValue("PRH")
                        .build(),
                    MetadataEntity.builder()
                        .withKey("process")
                        .withValue("APA")
                        .build()
                ))
                .withDefaultValues(Set.of(
                    DefaultValueEntity.builder()
                        .withFieldName("name")
                        .withValue("Kalle")
                        .build()
                ))
                .build();

            dbIntegration.saveTemplate(tmpl);

            var test = TemplateEntity.builder()
                .withIdentifier("test.template")
                .withName("Test")
                .withContent("PGRpdiBzdHlsZT0icG9zaXRpb246IGFic29sdXRlOyByaWdodDogMDsgYm90dG9tOiAyNXB4OyI+e3tjYXNlTnVtYmVyfX08L2Rpdj4=")
                .build();

            dbIntegration.saveTemplate(test);

            var test2 = TemplateEntity.builder()
                .withIdentifier("test.template")
                .withName("Test")
                .withContent("PGRpdiBzdHlsZT0icG9zaXRpb246IGFic29sdXRlOyB3b3JkLWJyZWFrOiBrZWVwLWFsbDsgcmlnaHQ6IDA7IGJvdHRvbTogMjVweDsiPnt7Y2FzZU51bWJlcn19PC9kaXY+")
                .build().withVersion(Version.parse("1.1"));

            dbIntegration.saveTemplate(test2);

/*
            var tmpl2 = TemplateEntity.builder()
                .withIdentifier("some.other.random.identifier")
                .withName("Some other template")
                .withDescription("Other template description goes here...")
                .withContent("otherContent...")
                .withMetadata(List.of(
                    MetadataEntity.builder()
                        .withKey("verksamhet")
                        .withValue("SBK")
                        .build(),
                    MetadataEntity.builder()
                        .withKey("process")
                        .withValue("PRH")
                        .build(),
                    MetadataEntity.builder()
                        .withKey("process")
                        .withValue("APA2")
                        .build()
                ))
                .build();

            dbIntegration.saveTemplate(tmpl);
            dbIntegration.saveTemplate(tmpl2);

            var searchMetadata = List.of(
                KeyValue.of("verksamhet", "SBK"),
                //KeyValue.of("process", "PRH"),
                KeyValue.of("process", "APA")
            );

            dbIntegration.findTemplate(searchMetadata).ifPresent(templateEntity -> {
                System.err.println("Template id:          " + templateEntity.getId());
                System.err.println("Template identifier:  " + templateEntity.getIdentifier());
                System.err.println("Template name:        " + templateEntity.getName());
                System.err.println("Template description: " + templateEntity.getDescription());
                System.err.println("Template metadata:");
                templateEntity.getMetadata().forEach(metadata -> {
                    System.err.println("  Key: " + metadata.getKey());
                    System.err.println("  Value: " + metadata.getValue());
                    System.err.println();
                });
                System.err.println("Template default values:");
                templateEntity.getDefaultValues().forEach(defaultValue -> {
                    System.err.println("  Field name: " + defaultValue.getFieldName());
                    System.err.println("  Value:      " + defaultValue.getValue());
                    System.err.println();
                });
            });
            */
        };
    }

    @Bean
    @Profile("default2")
    CommandLineRunner testStuff2(final DbIntegration dbIntegration,
            @Value("classpath:/templates/test/menu.peb") final Resource menuExampleTemplateResource,
            @Value("classpath:/templates/test/squirrel.peb") final Resource squirrelTemplateResource,
            @Value("classpath:/templates/test/citizenChanges.peb") final Resource citizenChangesExampleTemplateResource) {
        return args -> {
            var citizenChangesExampleTemplate = TemplateEntity.builder()
                .withIdentifier("example.citizen-changes")
                .withName("CitizenChanges e-mail")
                .withContent(asString(citizenChangesExampleTemplateResource, StandardCharsets.UTF_8))
                .build();
            var menuExampleTemplate = TemplateEntity.builder()
                .withIdentifier("example.menu")
                .withName("Menu (dummy)")
                .withContent(asString(menuExampleTemplateResource, StandardCharsets.UTF_8))
                .build();
            var squirrel = TemplateEntity.builder()
                .withIdentifier("resource.squirrel")
                .withName("Squirrel")
                .withContent(asString(squirrelTemplateResource, StandardCharsets.UTF_8))
                .build();

            dbIntegration.saveTemplate(citizenChangesExampleTemplate);
            dbIntegration.saveTemplate(menuExampleTemplate);
            dbIntegration.saveTemplate(squirrel);
        };
    }
}
