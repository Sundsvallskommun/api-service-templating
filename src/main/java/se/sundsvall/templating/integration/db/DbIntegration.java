package se.sundsvall.templating.integration.db;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.zalando.problem.Problem;
import org.zalando.problem.Status;

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

    public TemplateEntity saveTemplate(final TemplateEntity templateEntity) {
        return templateRepository.save(templateEntity);
    }

    public void deleteTemplate(final String templateId) {
        if (!templateRepository.existsById(templateId)) {
            throw Problem.valueOf(Status.NOT_FOUND, "Unable to find template with id '" + templateId + "'");
        }

        templateRepository.deleteById(templateId);
    }
}
