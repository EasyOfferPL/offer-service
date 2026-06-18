package pl.easyoffer.offer_service.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pl.easyoffer.offer_service.model.entity.OfferEntity;
import pl.easyoffer.offer_service.repository.projection.CategoryStatisticProjection;

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
                 select oe.category as categoryName, count(oe) as offerCount
                 from OfferEntity oe
                 group by oe.category
                 order by count(oe) desc
            """)
    List<CategoryStatisticProjection> getCategoryStatistics(Pageable pageable);

}
