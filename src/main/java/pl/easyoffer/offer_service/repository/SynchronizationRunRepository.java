package pl.easyoffer.offer_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import pl.easyoffer.offer_service.model.entity.SynchronizationRunEntity;
import pl.easyoffer.offer_service.repository.projection.SynchronizationCollectionRangeProjection;
import pl.easyoffer.offer_service.repository.projection.SynchronizationRunDatasetExportProjection;

import java.util.stream.Stream;

public interface SynchronizationRunRepository extends JpaRepository<SynchronizationRunEntity, Long> {

    @QueryHints({
            @jakarta.persistence.QueryHint(name = "org.hibernate.fetchSize", value = "1000"),
            @jakarta.persistence.QueryHint(name = "org.hibernate.readOnly", value = "true")
    })
    @Query("""
            SELECT run.id AS id,
                   run.cycleId AS cycleId,
                   run.source AS source,
                   run.status AS status,
                   run.startedAt AS startedAt,
                   run.finishedAt AS finishedAt,
                   run.fetchedOffers AS fetchedOffers,
                   run.persistedOffers AS persistedOffers,
                   run.createdOffers AS createdOffers,
                   run.updatedOffers AS updatedOffers,
                   run.skippedOffers AS skippedOffers,
                   run.errorType AS errorType,
                   CASE WHEN run.errorMessage IS NULL THEN false ELSE true END AS errorMessagePresent
            FROM SynchronizationRunEntity run
            ORDER BY run.id
            """)
    Stream<SynchronizationRunDatasetExportProjection> streamDatasetExportRows();

    @Query("""
            SELECT MIN(run.startedAt) AS startedAt, MAX(run.finishedAt) AS endedAt
            FROM SynchronizationRunEntity run
            """)
    SynchronizationCollectionRangeProjection findCollectionRange();
}
