package se.sundsvall.templating;

import static se.sundsvall.dept44.util.ResourceUtils.asString;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.Resource;

import se.sundsvall.templating.integration.db.DbIntegration;
import se.sundsvall.templating.integration.db.TemplateRepository;
import se.sundsvall.templating.integration.db.entity.Metadata;
import se.sundsvall.templating.integration.db.entity.TemplateEntity;

@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    CommandLineRunner testStuff(final DbIntegration dbIntegration,
            final TemplateRepository templateRepository,
            @Value("classpath:/templates/template1.peb") final Resource templateResource1,
            @Value("classpath:/templates/template2.peb") final Resource templateResource2,
            @Value("classpath:/templates/template3.peb") final Resource templateResource3,
            @Value("classpath:/templates/menu.peb") final Resource menuExampleTemplateResource,
            @Value("classpath:/templates/squirrel.peb") final Resource squirrelTemplateResource,
            @Value("classpath:/templates/citizenChanges.peb") final Resource citizenChangesExampleTemplateResource,
            @Value("classpath:/templates/logo.peb") final Resource logoTemplateResource,
            @Value("classpath:/templates/avslag.peb") final Resource avslagTemplateResource,
            @Value("classpath:/templates/bifall.peb") final Resource bifallTemplateResource) {
        return args -> {
            var template1 = TemplateEntity.builder()
                .withIdentifier("test.template1")
                .withName("Template 1")
                .withVariants(Map.of(
                    TemplateFlavor.TEXT, asString(templateResource1, StandardCharsets.UTF_8)
                ))
                .build();
System.err.println("TEMPLATE 1 ID: " + template1.getId());
            var template2 = TemplateEntity.builder()
                .withIdentifier("test.template2")
                .withName("Template 2")
                .withVariants(Map.of(
                    TemplateFlavor.TEXT, asString(templateResource2, StandardCharsets.UTF_8)
                ))
                .build();
            var template3 = TemplateEntity.builder()
                .withIdentifier("test.template3")
                .withName("Template 3")
                .withVariants(Map.of(
                    TemplateFlavor.TEXT, asString(templateResource3, StandardCharsets.UTF_8)
                ))
                .build();
            dbIntegration.saveTemplate(template1);
            dbIntegration.saveTemplate(template2);
            dbIntegration.saveTemplate(template3);

            var menuExampleTemplate = TemplateEntity.builder()
                .withIdentifier("example.menu")
                .withName("Menu (dummy)")
                .withVariants(Map.of(
                    TemplateFlavor.HTML, asString(menuExampleTemplateResource, StandardCharsets.UTF_8)
                ))
                .build();
            var squirrel = TemplateEntity.builder()
                .withIdentifier("resource.squirrel")
                .withName("Squirrel")
                .withVariants(Map.of(
                    TemplateFlavor.HTML, asString(squirrelTemplateResource, StandardCharsets.UTF_8)
                ))
                .build();
            var citizenChangesExampleTemplate = TemplateEntity.builder()
                .withIdentifier("example.citizen-changes")
                .withName("CitizenChanges e-mail")
                .withVariants(Map.of(
                    TemplateFlavor.HTML, asString(citizenChangesExampleTemplateResource, StandardCharsets.UTF_8)
                ))
                .build();

            dbIntegration.saveTemplate(menuExampleTemplate);
            dbIntegration.saveTemplate(squirrel);
            dbIntegration.saveTemplate(citizenChangesExampleTemplate);

            var logo = TemplateEntity.builder()
                .withIdentifier("resource.logo")
                .withName("Sundsvalls Kommun Logo")
                .withVariants(Map.of(
                    TemplateFlavor.HTML, asString(logoTemplateResource, StandardCharsets.UTF_8)
                ))
                .build();
            var bifallTemplate = TemplateEntity.builder()
                .withIdentifier("example.bifall")
                .withName("P-tillstånd Bifall")
                .withVariants(Map.of(
                    TemplateFlavor.HTML, asString(bifallTemplateResource, StandardCharsets.UTF_8)
                ))
                .build();
            var avslagTemplate = TemplateEntity.builder()
                .withIdentifier("example.avslag")
                .withName("P-tillstånd Avslag")
                .withVariants(Map.of(
                    TemplateFlavor.HTML, asString(avslagTemplateResource, StandardCharsets.UTF_8)
                ))
                .build();

            dbIntegration.saveTemplate(logo);
            dbIntegration.saveTemplate(bifallTemplate);
            dbIntegration.saveTemplate(avslagTemplate);

            var tmpl = TemplateEntity.builder()
                .withIdentifier("Some random identifier goes here...")
                .withName("Some template")
                .withDescription("Template description goes here...")
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
        };
    }
}
