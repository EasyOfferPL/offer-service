package pl.easyoffer.offer_service.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pl.easyoffer.offer_service.model.entity.OfferEntity;
import pl.easyoffer.offer_service.repository.projection.CategoryProjection;
import pl.easyoffer.offer_service.repository.projection.CategoryStatisticProjection;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface OfferRepository extends JpaRepository<OfferEntity, Long>, JpaSpecificationExecutor<OfferEntity> {

    Optional<OfferEntity> findByExternalId(String externalId);

    @Query("""
            SELECT oe
            FROM OfferEntity oe
            WHERE lower(oe.title) = lower(:title)
              AND lower(oe.companyName) = lower(:companyName)
              AND ((:location IS NULL AND oe.location IS NULL) OR lower(oe.location) = lower(:location))
            """)
    Optional<OfferEntity> findByCoreFields(
            @Param("title") String title,
            @Param("companyName") String companyName,
            @Param("location") String location
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
            SELECT oe
            FROM OfferEntity oe
            WHERE (oe.salaryMin IS NOT NULL OR oe.salaryMax IS NOT NULL)
              AND oe.category = :categoryName
              AND oe.currency = :currency
              AND oe.employmentType = :employmentType
              AND (:experienceLevel IS NULL OR oe.experienceLevel = :experienceLevel)
              AND COALESCE(oe.publishedAt, oe.createdAt) BETWEEN :dateFrom AND :dateTo
            """)
    List<OfferEntity> findForSalaryTrend(
            @Param("categoryName") String categoryName,
            @Param("currency") String currency,
            @Param("employmentType") String employmentType,
            @Param("experienceLevel") String experienceLevel,
            @Param("dateFrom") LocalDateTime dateFrom,
            @Param("dateTo") LocalDateTime dateTo
    );

}
