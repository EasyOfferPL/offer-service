package pl.easyoffer.offer_service.service.synchronizer;

public record SynchronizationResult(int fetchedOffers, int persistedOffers) {

    public SynchronizationResult {
        if (fetchedOffers < 0 || persistedOffers < 0) {
            throw new IllegalArgumentException("Synchronization counts cannot be negative");
        }
        if (persistedOffers > fetchedOffers) {
            throw new IllegalArgumentException("Persisted offer count cannot exceed fetched offer count");
        }
    }
}
