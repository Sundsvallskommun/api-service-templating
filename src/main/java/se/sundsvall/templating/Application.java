package se.sundsvall.templating;

import static se.sundsvall.dept44.util.ResourceUtils.asString;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.Resource;

import se.sundsvall.templating.integration.db.DbIntegration;
import se.sundsvall.templating.integration.db.entity.TemplateEntity;

@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    CommandLineRunner testStuff(final DbIntegration dbIntegration,
            @Value("classpath:/templates/template1.peb") final Resource templateResource1,
            @Value("classpath:/templates/template2.peb") final Resource templateResource2,
            @Value("classpath:/templates/template3.peb") final Resource templateResource3,
            @Value("classpath:/templates/menu.peb") final Resource menuExampleTemplateResource,
            @Value("classpath:/templates/squirrel.peb") final Resource squirrelTemplateResource,
            @Value("classpath:/templates/citizenChanges.peb") final Resource citizenChangesExampleTemplateResource) {
        return args -> {
            var template1 = TemplateEntity.builder()
                .withId("test.template1")
                .withName("Template 1")
                .withVariants(Map.of(
                    TemplateFlavor.TEXT, asString(templateResource1, StandardCharsets.UTF_8)
                ))
                .build();
            var template2 = TemplateEntity.builder()
                .withId("test.template2")
                .withName("Template 2")
                .withVariants(Map.of(
                    TemplateFlavor.TEXT, asString(templateResource2, StandardCharsets.UTF_8)
                ))
                .build();
            var template3 = TemplateEntity.builder()
                .withId("test.template3")
                .withName("Template 3")
                .withVariants(Map.of(
                    TemplateFlavor.TEXT, asString(templateResource3, StandardCharsets.UTF_8)
                ))
                .build();
            dbIntegration.saveTemplate(template1);
            dbIntegration.saveTemplate(template2);
            dbIntegration.saveTemplate(template3);

            var menuExampleTemplate = TemplateEntity.builder()
                .withId("example.menu")
                .withName("Menu (dummy)")
                .withVariants(Map.of(
                    TemplateFlavor.HTML, asString(menuExampleTemplateResource, StandardCharsets.UTF_8)
                ))
                .build();
            var squirrel = TemplateEntity.builder()
                .withId("resource.squirrel")
                .withName("Squirrel")
                .withVariants(Map.of(
                    TemplateFlavor.HTML, asString(squirrelTemplateResource, StandardCharsets.UTF_8)
                ))
                .build();
            var citizenChangesExampleTemplate = TemplateEntity.builder()
                .withId("example.citizen-changes")
                .withName("CitizenChanges e-mail")
                .withVariants(Map.of(
                    TemplateFlavor.HTML, asString(citizenChangesExampleTemplateResource, StandardCharsets.UTF_8)
                ))
                .build();

            dbIntegration.saveTemplate(menuExampleTemplate);
            dbIntegration.saveTemplate(squirrel);
            dbIntegration.saveTemplate(citizenChangesExampleTemplate);
        };
    }
}
