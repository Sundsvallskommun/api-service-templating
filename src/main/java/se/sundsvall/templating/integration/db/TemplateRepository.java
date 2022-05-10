package se.sundsvall.templating.integration.db;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import se.sundsvall.templating.integration.db.entity.TemplateEntity;

public interface TemplateRepository extends JpaRepository<TemplateEntity, String>, JpaSpecificationExecutor<TemplateEntity> {

    Optional<TemplateEntity> findTemplateEntityByIdentifierEquals(String identifier);
}
