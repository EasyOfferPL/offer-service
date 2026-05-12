package pl.easyoffer.offer_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pl.easyoffer.offer_service.model.entity.JobOfferEntity;

import java.util.Optional;

public interface JobOfferRepository extends JpaRepository<JobOfferEntity, Long>, JpaSpecificationExecutor<JobOfferEntity> {

    Optional<JobOfferEntity> findByExternalId(String externalId);

    @Query("""
            SELECT jo
            FROM JobOfferEntity jo
            WHERE lower(jo.title) = lower(:title)
              AND lower(jo.companyName) = lower(:companyName)
              AND ((:location IS NULL AND jo.location IS NULL) OR lower(jo.location) = lower(:location))
            """)
    Optional<JobOfferEntity> findByCoreFields(
            @Param("title") String title,
            @Param("companyName") String companyName,
            @Param("location") String location
    );

}
