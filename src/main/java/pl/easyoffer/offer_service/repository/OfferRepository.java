package pl.easyoffer.offer_service.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import pl.easyoffer.offer_service.model.SalaryType;
import pl.easyoffer.offer_service.model.entity.OfferEntity;
import pl.easyoffer.offer_service.repository.projection.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public interface OfferRepository extends JpaRepository<OfferEntity, Long>, JpaSpecificationExecutor<OfferEntity> {

    Optional<OfferEntity> findBySourceAndExternalId(String source, String externalId);

    Optional<OfferEntity> findFirstBySourceAndExternalIdIsNullAndDeduplicationFingerprintOrderByIdAsc(
            String source,
            String deduplicationFingerprint
    );

    @Query("""
            SELECT groupedOffer.deduplicationFingerprint
            FROM OfferEntity groupedOffer
            WHERE groupedOffer.deduplicationFingerprint IS NOT NULL
            GROUP BY groupedOffer.deduplicationFingerprint
            HAVING COUNT(DISTINCT groupedOffer.source) > 1
            ORDER BY groupedOffer.deduplicationFingerprint
            """)
    List<String> findCrossSourceDuplicateFingerprints(Pageable pageable);

    @Query(value = """
            SELECT COUNT(*)
            FROM (
                SELECT deduplication_fingerprint
                FROM offers
                WHERE deduplication_fingerprint IS NOT NULL
                GROUP BY deduplication_fingerprint
                HAVING COUNT(DISTINCT source) > 1
            ) cross_source_groups
            """, nativeQuery = true)
    long countCrossSourceDuplicateGroups();

    @Query(value = """
            SELECT COUNT(*)
            FROM offers
            WHERE deduplication_fingerprint IN (
                SELECT deduplication_fingerprint
                FROM offers
                WHERE deduplication_fingerprint IS NOT NULL
                GROUP BY deduplication_fingerprint
                HAVING COUNT(DISTINCT source) > 1
            )
            """, nativeQuery = true)
    long countCrossSourceDuplicateGroupMembers();

    @Query("""
            SELECT oe.id AS offerId,
                   oe.source AS source,
                   oe.externalId AS externalId,
                   oe.deduplicationFingerprint AS deduplicationFingerprint
            FROM OfferEntity oe
            WHERE oe.deduplicationFingerprint IN :fingerprints
            ORDER BY oe.deduplicationFingerprint, oe.source, oe.id
            """)
    List<DeduplicationAuditOfferProjection> findCrossSourceDuplicateGroupMembers(
            @Param("fingerprints") List<String> fingerprints
    );

    @Query(value = """
             SELECT oe.category AS categoryName, count(oe) AS offerCount
             FROM OfferEntity oe
             GROUP BY oe.category
             ORDER BY count(oe) DESC
            """)
    List<CategoryStatisticProjection> getCategoryStatistics(Pageable pageable);

    @Query("""
            SELECT DISTINCT oe.category AS categoryName
            FROM OfferEntity oe
            WHERE oe.category IS NOT NULL
    """)
    List<CategoryProjection> getCategories();

    @Query("""
            SELECT oe
            FROM OfferEntity oe
            WHERE (oe.salaryMin IS NOT NULL OR oe.salaryMax IS NOT NULL)
              AND (:experienceLevel IS NULL OR oe.experienceLevel = :experienceLevel)
              AND (:categoryName IS NULL OR oe.category = :categoryName)
            """)
    List<OfferEntity> findForSalaryAnalytics(
            @Param("categoryName") String categoryName,
            @Param("experienceLevel") String experienceLevel
    );

    @Query("""
            SELECT DISTINCT offer
            FROM OfferEntity offer
            LEFT JOIN FETCH offer.technologies
            WHERE (:categoryName IS NULL OR offer.category = :categoryName)
              AND offer.publishedAt BETWEEN :dateFrom AND :dateTo
            ORDER BY offer.id
            """)
    List<OfferEntity> findForTechnologyAnalytics(
            @Param("categoryName") String categoryName,
            @Param("dateFrom") LocalDateTime dateFrom,
            @Param("dateTo") LocalDateTime dateTo
    );

    @Query("""
            SELECT DISTINCT offer
            FROM OfferEntity offer
            LEFT JOIN FETCH offer.technologies
            WHERE (:categoryName IS NULL OR offer.category = :categoryName)
              AND offer.source IN :sources
              AND offer.publishedAt BETWEEN :dateFrom AND :dateTo
            ORDER BY offer.id
            """)
    List<OfferEntity> findForTechnologyAnalytics(
            @Param("categoryName") String categoryName,
            @Param("sources") List<String> sources,
            @Param("dateFrom") LocalDateTime dateFrom,
            @Param("dateTo") LocalDateTime dateTo
    );

    @Query("""
            SELECT oe.salaryMin AS salaryMin,
                   oe.salaryMax AS salaryMax,
                   oe.currency AS currency,
                   oe.salaryUnit AS salaryUnit,
                   oe.employmentType AS employmentType,
                   oe.salaryType AS salaryType
            FROM OfferEntity oe
            """)
    Stream<SalaryComparabilityInputProjection> streamSalaryComparabilityInputs();

    @QueryHints({
            @jakarta.persistence.QueryHint(name = "org.hibernate.fetchSize", value = "1000"),
            @jakarta.persistence.QueryHint(name = "org.hibernate.readOnly", value = "true")
    })
    @Query("""
            SELECT offer.id AS id,
                   offer.externalId AS externalId,
                   offer.title AS title,
                   offer.companyName AS companyName,
                   offer.location AS location,
                   offer.experienceLevel AS experienceLevel,
                   offer.employmentType AS employmentType,
                   offer.workMode AS workMode,
                   offer.workingTime AS workingTime,
                   offer.salaryMin AS salaryMin,
                   offer.salaryMax AS salaryMax,
                   offer.salaryUnit AS salaryUnit,
                   offer.currency AS currency,
                   offer.salaryType AS salaryType,
                   offer.source AS source,
                   offer.deduplicationFingerprint AS deduplicationFingerprint,
                   offer.url AS url,
                   offer.language AS language,
                   offer.category AS category,
                   offer.publishedAt AS publishedAt,
                   offer.expiredAt AS expiredAt,
                   offer.openToHireUkrainians AS openToHireUkrainians,
                   offer.version AS version,
                   offer.createdAt AS createdAt,
                   offer.updatedAt AS updatedAt
            FROM OfferEntity offer
            ORDER BY offer.id
            """)
    Stream<OfferDatasetExportProjection> streamDatasetExportRows();

    @QueryHints({
            @jakarta.persistence.QueryHint(name = "org.hibernate.fetchSize", value = "1000"),
            @jakarta.persistence.QueryHint(name = "org.hibernate.readOnly", value = "true")
    })
    @Query("""
            SELECT offer.id AS offerId, technology.id AS technologyId
            FROM OfferEntity offer
            JOIN offer.technologies technology
            ORDER BY offer.id, technology.id
            """)
    Stream<OfferTechnologyDatasetExportProjection> streamTechnologyDatasetExportRows();

    @Query("""
            SELECT offer.source AS source, COUNT(offer) AS offerCount
            FROM OfferEntity offer
            GROUP BY offer.source
            ORDER BY offer.source
            """)
    List<OfferSourceCountProjection> countOffersBySource();

    @Query("""
            SELECT oe
            FROM OfferEntity oe
            WHERE (oe.salaryMin IS NOT NULL OR oe.salaryMax IS NOT NULL)
              AND oe.category = :categoryName
              AND UPPER(TRIM(oe.currency)) = :currency
              AND UPPER(TRIM(oe.employmentType)) = :employmentType
              AND oe.salaryType = :salaryType
              AND (:experienceLevel IS NULL OR oe.experienceLevel = :experienceLevel)
              AND COALESCE(oe.publishedAt, oe.createdAt) BETWEEN :dateFrom AND :dateTo
            """)
    List<OfferEntity> findForSalaryTrend(
            @Param("categoryName") String categoryName,
            @Param("currency") String currency,
            @Param("employmentType") String employmentType,
            @Param("salaryType") SalaryType salaryType,
            @Param("experienceLevel") String experienceLevel,
            @Param("dateFrom") LocalDateTime dateFrom,
            @Param("dateTo") LocalDateTime dateTo
    );

}
