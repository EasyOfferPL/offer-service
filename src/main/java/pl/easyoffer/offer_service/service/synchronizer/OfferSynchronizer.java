package pl.easyoffer.offer_service.service.synchronizer;

import pl.easyoffer.offer_service.model.JobOfferSourceType;

public interface OfferSynchronizer {

    JobOfferSourceType getSource();

    SynchronizationResult synchronize();
}
