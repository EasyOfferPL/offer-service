package pl.easyoffer.offer_service.repository.projection;

import pl.easyoffer.offer_service.model.JobOfferSourceType;
import pl.easyoffer.offer_service.model.SynchronizationRunStatus;

import java.time.Instant;
import java.util.UUID;

public interface SynchronizationRunDatasetExportProjection extends DatasetExportRow {

    Long getId();
    UUID getCycleId();
    JobOfferSourceType getSource();
    SynchronizationRunStatus getStatus();
    Instant getStartedAt();
    Instant getFinishedAt();
    Integer getFetchedOffers();
    Integer getPersistedOffers();
    Integer getCreatedOffers();
    Integer getUpdatedOffers();
    Integer getSkippedOffers();
    String getErrorType();
    Boolean getErrorMessagePresent();

    @Override
    default Object[] values() {
        return new Object[]{
                getId(), getCycleId(), getSource(), getStatus(), getStartedAt(), getFinishedAt(),
                getFetchedOffers(), getPersistedOffers(), getCreatedOffers(), getUpdatedOffers(),
                getSkippedOffers(), getErrorType(), getErrorMessagePresent()
        };
    }
}
