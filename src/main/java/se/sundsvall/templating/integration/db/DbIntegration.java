package se.sundsvall.templating.integration.db;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.zalando.problem.Problem;
import org.zalando.problem.Status;

import se.sundsvall.templating.domain.KeyValue;
import se.sundsvall.templating.integration.db.entity.TemplateEntity;
import se.sundsvall.templating.integration.db.spec.Specifications;

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
    public Optional<TemplateEntity> getTemplate(final String identifier) {
        return templateRepository.findByIdentifier(identifier);
    }

    @Transactional(readOnly = true)
    public Optional<TemplateEntity> findTemplate(final List<KeyValue> metadata) {
        try {
            return templateRepository.findOne(toTemplateEntitySpecification(metadata));
        } catch (IncorrectResultSizeDataAccessException e) {
            throw Problem.valueOf(Status.NOT_FOUND, "Metadata query resulted in multiple matching templates");
        }
    }

    @Transactional(readOnly = true)
    public List<TemplateEntity> findTemplates(final List<KeyValue> metadata) {
        return templateRepository.findAll(toTemplateEntitySpecification(metadata));
    }

    Specification<TemplateEntity> toTemplateEntitySpecification(final List<KeyValue> metadata) {
        var specifications = new ArrayList<>(metadata.stream()
            .map(entry -> Specifications.hasMetadata(entry.getKey(), entry.getValue()))
            .toList());

        if (specifications.isEmpty()) {
            throw new IllegalStateException("No metadata specifications supplied");
        }

        var specification = specifications.remove(0);
        for (var additionalSpecification : specifications) {
            specification = specification.and(additionalSpecification);
        }
        return specification;
    }

    public TemplateEntity saveTemplate(final TemplateEntity templateEntity) {
        return templateRepository.save(templateEntity);
    }

    public void deleteTemplate(final String identifier) {
        if (!templateRepository.existsByIdentifier(identifier)) {
            throw Problem.valueOf(Status.NOT_FOUND, "Unable to find template with identifier '" + identifier + "'");
        }

        templateRepository.deleteByIdentifier(identifier);
    }
}
