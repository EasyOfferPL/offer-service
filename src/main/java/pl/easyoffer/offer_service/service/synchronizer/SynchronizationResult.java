package pl.easyoffer.offer_service.service.synchronizer;

import pl.easyoffer.offer_service.service.persistence.OfferPersistenceOutcome;

import java.util.Collection;

public record SynchronizationResult(
        int fetchedOffers,
        int createdOffers,
        int updatedOffers,
        int skippedOffers
) {

    public SynchronizationResult {
        if (fetchedOffers < 0 || createdOffers < 0 || updatedOffers < 0 || skippedOffers < 0) {
            throw new IllegalArgumentException("Synchronization counts cannot be negative");
        }
        if ((long) createdOffers + updatedOffers + skippedOffers != fetchedOffers) {
            throw new IllegalArgumentException(
                    "Fetched offer count must equal created, updated and skipped offer counts"
            );
        }
    }

    public static SynchronizationResult fromOutcomes(Collection<OfferPersistenceOutcome> outcomes) {
        int createdOffers = 0;
        int updatedOffers = 0;
        int skippedOffers = 0;

        for (OfferPersistenceOutcome outcome : outcomes) {
            switch (outcome) {
                case CREATED -> createdOffers++;
                case UPDATED -> updatedOffers++;
                case SKIPPED -> skippedOffers++;
            }
        }

        return new SynchronizationResult(
                outcomes.size(), createdOffers, updatedOffers, skippedOffers
        );
    }

    public static SynchronizationResult allSkipped(int fetchedOffers) {
        return new SynchronizationResult(fetchedOffers, 0, 0, fetchedOffers);
    }

    public int persistedOffers() {
        return Math.addExact(createdOffers, updatedOffers);
    }
}
