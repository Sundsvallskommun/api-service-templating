package se.sundsvall.templating.integration.db;

import org.springframework.data.jpa.repository.JpaRepository;

import se.sundsvall.templating.integration.db.entity.TemplateEntity;

interface TemplateRepository extends JpaRepository<TemplateEntity, String> {

}
