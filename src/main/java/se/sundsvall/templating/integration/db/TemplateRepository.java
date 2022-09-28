package se.sundsvall.templating.integration.db;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import se.sundsvall.templating.integration.db.entity.TemplateEntity;

public interface TemplateRepository extends JpaRepository<TemplateEntity, String>, JpaSpecificationExecutor<TemplateEntity> {

    boolean existsByIdentifier(String identifier);

    @Query("from TemplateEntity t where t.identifier = :identifier")
    Optional<TemplateEntity> findByIdentifier(@Param("identifier") String identifier);

    @Modifying
    @Query("delete from TemplateEntity t where t.identifier = :identifier")
    void deleteByIdentifier(@Param("identifier") String identifier);
}
