package pl.easyoffer.offer_service.service.deduplication;

import pl.easyoffer.offer_service.model.entity.OfferEntity;
import pl.easyoffer.offer_service.model.to.OfferRequestTO;

import java.util.Optional;

public interface DuplicateSearchStrategy {

    int priority();

    Optional<OfferEntity> find(OfferRequestTO request);

    default String getClassName() {
        return this.getClass().getSimpleName();
    }

}
