package se.sundsvall.templating.integration.db;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.zalando.problem.Problem;
import org.zalando.problem.Status;

import se.sundsvall.templating.integration.db.entity.Specifications;
import se.sundsvall.templating.integration.db.entity.TemplateEntity;

@Component
@Transactional
public class DbIntegration {

    private final TemplateRepository templateRepository;

    public DbIntegration(final TemplateRepository templateRepository) {
        this.templateRepository = templateRepository;
    }

    @Transactional(readOnly = true)
    public List<TemplateEntity> getAllTemplates() {
        return templateRepository.findAll();
    }

    @Transactional(readOnly = true)
    public boolean templateExists(final String templateId) {
        return templateRepository.existsById(templateId);
    }

    @Transactional(readOnly = true)
    public Optional<TemplateEntity> getTemplate(final String templateId) {
        return templateRepository.findById(templateId);
    }

    @Transactional(readOnly = true)
    public Optional<TemplateEntity> getTemplateByIdentifier(final String identifier) {
        return templateRepository.findTemplateEntityByIdentifierEquals(identifier);
    }

    @Transactional(readOnly = true)
    public Optional<TemplateEntity> findTemplate(final List<KeyValue> metadata) {
        var specifications = new ArrayList<>(metadata.stream()
            .map(entry -> Specifications.hasMetadata(entry.key, entry.value))
            .toList());

        if (specifications.isEmpty()) {
            throw new IllegalStateException("No metadata specifications supplied");
        }

        var specification = specifications.remove(0);
        for (var additionalSpecification : specifications) {
            specification = specification.and(additionalSpecification);
        }

        return templateRepository.findOne(specification);
    }

    public TemplateEntity saveTemplate(final TemplateEntity templateEntity) {
        return templateRepository.save(templateEntity);
    }

    public void deleteTemplate(final String templateId) {
        if (!templateRepository.existsById(templateId)) {
            throw Problem.valueOf(Status.NOT_FOUND, "Unable to find template with id '" + templateId + "'");
        }

        templateRepository.deleteById(templateId);
    }

    public static class KeyValue {

        private final String key;
        private final String value;

        private KeyValue(final String key, final String value) {
            this.key = key;
            this.value = value;
        }

        public static KeyValue of(final String key, final String value) {
            return new KeyValue(key, value);
        }
    }
}
