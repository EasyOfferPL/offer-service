package pl.easyoffer.offer_service.repository.projection;

import java.time.Instant;

public interface SynchronizationCollectionRangeProjection {

    Instant getStartedAt();
    Instant getEndedAt();
}
