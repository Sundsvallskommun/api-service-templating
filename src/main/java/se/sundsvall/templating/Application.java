package se.sundsvall.templating;

import static se.sundsvall.dept44.util.ResourceUtils.asString;

import java.nio.charset.StandardCharsets;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.Resource;

import se.sundsvall.templating.integration.db.DbIntegration;
import se.sundsvall.templating.integration.db.entity.TemplateEntity;

@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    CommandLineRunner testStuff1(final DbIntegration dbIntegration,
            @Value("classpath:/templates/logo.peb") final Resource logoTemplateResource,
            @Value("classpath:/templates/avslag.peb") final Resource avslagTemplateResource,
            @Value("classpath:/templates/bifall.peb") final Resource bifallTemplateResource) {
        return args -> {
            var bifallTemplate = TemplateEntity.builder()
                .withIdentifier("example.bifall")
                .withName("P-tillstånd Bifall")
                .withContent(asString(bifallTemplateResource, StandardCharsets.UTF_8))
                .build();
            dbIntegration.saveTemplate(bifallTemplate);

            var avslagTemplate = TemplateEntity.builder()
                .withIdentifier("example.avslag")
                .withName("P-tillstånd Avslag")
                .withContent(asString(avslagTemplateResource, StandardCharsets.UTF_8))
                .build();
            dbIntegration.saveTemplate(avslagTemplate);

            var logo = TemplateEntity.builder()
                .withIdentifier("common.resources.sundsvalls-kommun-logo")
                .withName("Sundsvalls Kommun Logo")
                .withContent(asString(logoTemplateResource, StandardCharsets.UTF_8))
                .build();
            dbIntegration.saveTemplate(logo);

            /*
            var tmpl = TemplateEntity.builder()
                .withIdentifier("Some random identifier goes here...")
                .withName("Some template")
                .withDescription("Template description goes here...")
                .withContent("someContent...")
                .withMetadata(List.of(
                    Metadata.builder()
                        .withKey("verksamhet")
                        .withValue("SBK")
                        .build(),
                    Metadata.builder()
                        .withKey("process")
                        .withValue("PRH")
                        .build(),
                    Metadata.builder()
                        .withKey("process")
                        .withValue("APA")
                        .build()
                ))
                .build();

            dbIntegration.saveTemplate(tmpl);

            var searchMetadata = List.of(
                DbIntegration.KeyValue.of("verksamhet", "SBK"),
                DbIntegration.KeyValue.of("process", "PRH"),
                DbIntegration.KeyValue.of("process", "APA")
            );

            dbIntegration.findTemplate(searchMetadata).ifPresent(templateEntity -> {
                System.err.println(templateEntity.getId());
                System.err.println(templateEntity.getName());
                System.err.println(templateEntity.getDescription());
                templateEntity.getMetadata().forEach(metadata -> {
                    System.err.println("  " + metadata.getId());
                    System.err.println("  " + metadata.getKey() + " => " + metadata.getValue());
                });
            });
            */
        };
    }

    @Bean
    @Profile("test")
    CommandLineRunner testStuff2(final DbIntegration dbIntegration,
            @Value("classpath:/templates/test/template1.peb") final Resource templateResource1,
            @Value("classpath:/templates/test/template2.peb") final Resource templateResource2,
            @Value("classpath:/templates/test/template3.peb") final Resource templateResource3,
            @Value("classpath:/templates/test/menu.peb") final Resource menuExampleTemplateResource,
            @Value("classpath:/templates/test/squirrel.peb") final Resource squirrelTemplateResource,
            @Value("classpath:/templates/test/citizenChanges.peb") final Resource citizenChangesExampleTemplateResource) {
        return args -> {
            var template1 = TemplateEntity.builder()
                    .withIdentifier("test.template1")
                    .withName("Template 1")
                    .withContent(asString(templateResource1, StandardCharsets.UTF_8))
                    .build();
            dbIntegration.saveTemplate(template1);

            var template2 = TemplateEntity.builder()
                    .withIdentifier("test.template2")
                    .withName("Template 2")
                    .withContent(asString(templateResource2, StandardCharsets.UTF_8))
                    .build();
            dbIntegration.saveTemplate(template2);

            var template3 = TemplateEntity.builder()
                    .withIdentifier("test.template3")
                    .withName("Template 3")
                    .withContent(asString(templateResource3, StandardCharsets.UTF_8))
                    .build();
            dbIntegration.saveTemplate(template3);

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
