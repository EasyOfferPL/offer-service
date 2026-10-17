package pl.easyoffer.offer_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pl.easyoffer.offer_service.model.entity.TechnologyEntity;
import pl.easyoffer.offer_service.repository.projection.TechnologyDatasetExportProjection;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public interface TechnologyRepository extends JpaRepository<TechnologyEntity, Long> {
    Optional<TechnologyEntity> findByNameAndLevel(String name, Integer level);

    long countByNormalizationVersion(String normalizationVersion);

    @Query("""
            SELECT COUNT(technology)
            FROM TechnologyEntity technology
            WHERE technology.normalizationVersion = :normalizationVersion
              AND technology.name <> technology.canonicalName
            """)
    long countChangedValues(@Param("normalizationVersion") String normalizationVersion);

    @Query(value = """
            SELECT COUNT(*)
            FROM job_offer_technologies offer_technology
            JOIN technologies technology ON technology.id = offer_technology.technology_id
            WHERE technology.normalization_version = :normalizationVersion
            """, nativeQuery = true)
    long countOfferAssociations(@Param("normalizationVersion") String normalizationVersion);

    @Query(value = """
            SELECT COUNT(*)
            FROM job_offer_technologies offer_technology
            JOIN technologies technology ON technology.id = offer_technology.technology_id
            WHERE technology.normalization_version = :normalizationVersion
              AND technology.name <> technology.canonical_name
            """, nativeQuery = true)
    long countChangedOfferAssociations(@Param("normalizationVersion") String normalizationVersion);

    @org.springframework.data.jpa.repository.QueryHints({
            @jakarta.persistence.QueryHint(name = "org.hibernate.fetchSize", value = "1000"),
            @jakarta.persistence.QueryHint(name = "org.hibernate.readOnly", value = "true")
    })
    @Query("""
            SELECT technology.id AS id,
                   technology.name AS name,
                   technology.canonicalName AS canonicalName,
                   technology.normalizationVersion AS normalizationVersion,
                   technology.level AS level
            FROM TechnologyEntity technology
            ORDER BY technology.id
            """)
    Stream<TechnologyDatasetExportProjection> streamDatasetExportRows();

    @Query("""
            SELECT DISTINCT technology.normalizationVersion
            FROM TechnologyEntity technology
            ORDER BY technology.normalizationVersion
            """)
    List<String> findDistinctNormalizationVersions();
}
