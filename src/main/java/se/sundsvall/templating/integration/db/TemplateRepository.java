package se.sundsvall.templating.integration.db;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import se.sundsvall.templating.integration.db.entity.TemplateEntity;
import se.sundsvall.templating.integration.db.entity.Version;

public interface TemplateRepository extends JpaRepository<TemplateEntity, String>, JpaSpecificationExecutor<TemplateEntity> {

    boolean existsByIdentifier(String identifier);

    @Query("""
        select case when count(t)> 0 then true else false end
        from TemplateEntity t
        where
        t.identifier = :identifier and
        t.version.major = :#{#version.major} and
        t.version.minor = :#{#version.minor}
    """)
    boolean existsByIdentifierAndVersion(@Param("identifier") String identifier, @Param("version") Version version);

    @Query("""
        from TemplateEntity t
        where
        t.identifier = :identifier and
        t.version.major = :#{#version.major} and
        t.version.minor = :#{#version.minor}
    """)
    Optional<TemplateEntity> findByIdentifierAndVersion(
        @Param("identifier") String identifier,
        @Param("version") Version version);

    default Optional<TemplateEntity> findLatestByIdentifier(@Param("identifier") String identifier) {
        return findLatestByIdentifier(identifier, PageRequest.of(0, 1)).stream().findFirst();
    }

    @Query("""
        from TemplateEntity t
        where
        t.identifier = :identifier
        order by t.version.major desc, t.version.minor desc
    """)
    Page<TemplateEntity> findLatestByIdentifier(@Param("identifier") String identifier, Pageable pageable);

    @Modifying
    @Query("delete from TemplateEntity t where t.identifier = :identifier")
    void deleteByIdentifier(@Param("identifier") String identifier);

    @Modifying
    @Query("""
        delete from TemplateEntity t
        where
        t.identifier = :identifier and
        t.version.major = :#{#version.major} and
        t.version.minor = :#{#version.minor}
    """)
    void deleteByIdentifierAndVersion(@Param("identifier") String identifier, @Param("version") Version version);
}
