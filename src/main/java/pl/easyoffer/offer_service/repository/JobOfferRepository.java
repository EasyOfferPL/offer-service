package pl.easyoffer.offer_service.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pl.easyoffer.offer_service.model.entity.JobOfferEntity;
import pl.easyoffer.offer_service.repository.projection.TechnologyCountProjection;

public interface JobOfferRepository extends JpaRepository<JobOfferEntity, Long>, JpaSpecificationExecutor<JobOfferEntity> {

    Optional<JobOfferEntity> findByExternalId(String externalId);

    @Query("""
            SELECT jo
            FROM JobOfferEntity jo
            WHERE lower(jo.title) = lower(:title)
              AND lower(jo.companyName) = lower(:companyName)
              AND ((:location IS NULL AND jo.location IS NULL) OR lower(jo.location) = lower(:location))
            """)
    Optional<JobOfferEntity> findDuplicateByCoreFields(
            @Param("title") String title,
            @Param("companyName") String companyName,
            @Param("location") String location
    );

    @Query("""
            SELECT t.name AS name, COUNT(jo.id) AS offerCount
            FROM JobOfferEntity jo
            JOIN jo.technologies t
            GROUP BY t.name
            ORDER BY COUNT(jo.id) DESC
            """)
    List<TechnologyCountProjection> findTopTechnologies(org.springframework.data.domain.Pageable pageable);

}
