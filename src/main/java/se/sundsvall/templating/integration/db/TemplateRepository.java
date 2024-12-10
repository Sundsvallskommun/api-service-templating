package se.sundsvall.templating.integration.db;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import java.util.List;
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

@CircuitBreaker(name = "templateRepository")
public interface TemplateRepository extends JpaRepository<TemplateEntity, String>, JpaSpecificationExecutor<TemplateEntity> {

	boolean existsByIdentifierAndMunicipalityId(String identifier, String municipalityId);

	@Query("""
		select case when count(t)> 0 then true else false end
		from TemplateEntity t
		where
		t.identifier = :identifier and
		t.municipalityId = :municipalityId and
		t.version.major = :#{#version.major} and
		t.version.minor = :#{#version.minor}
		""")
	boolean existsByIdentifierAndVersionAndMunicipalityId(@Param("identifier") String identifier, @Param("version") Version version, @Param("municipalityId") String municipalityId);

	@Query("""
		from TemplateEntity t
		where
		t.identifier = :identifier and
		t.municipalityId = :municipalityId and
		t.version.major = :#{#version.major} and
		t.version.minor = :#{#version.minor}
		""")
	Optional<TemplateEntity> findByIdentifierAndVersionAndMunicipalityId(
		@Param("identifier") String identifier,
		@Param("version") Version version,
		@Param("municipalityId") String municipalityId);

	@Query("""
		from TemplateEntity t
		where
		t.municipalityId = :municipalityId
		""")
	List<TemplateEntity> findAllByMunicipalityId(@Param("municipalityId") String municipalityId);

	default Optional<TemplateEntity> findLatestByIdentifierAndMunicipalityId(@Param("identifier") final String identifier, @Param("municipalityId") final String municipalityId) {
		return findLatestByIdentifierAndMunicipalityId(identifier, municipalityId, PageRequest.of(0, 1)).stream().findFirst();
	}

	@Query("""
		from TemplateEntity t
		where
		t.identifier = :identifier and
		t.municipalityId = :municipalityId
		order by t.version.major desc, t.version.minor desc
		""")
	Page<TemplateEntity> findLatestByIdentifierAndMunicipalityId(@Param("identifier") String identifier, @Param("municipalityId") String municipalityId, Pageable pageable);

	@Modifying
	@Query("""
		delete from TemplateEntity t
		where
		t.identifier = :identifier and
		t.municipalityId = :municipalityId
		""")
	void deleteByIdentifierAndMunicipalityId(@Param("identifier") String identifier, @Param("municipalityId") String municipalityId);

	@Modifying
	@Query("""
		delete from TemplateEntity t
		where
		t.identifier = :identifier and
		t.municipalityId = :municipalityId and
		t.version.major = :#{#version.major} and
		t.version.minor = :#{#version.minor}
		""")
	void deleteByIdentifierAndVersionAndMunicipalityId(@Param("identifier") String identifier, @Param("version") Version version, @Param("municipalityId") String municipalityId);
}
