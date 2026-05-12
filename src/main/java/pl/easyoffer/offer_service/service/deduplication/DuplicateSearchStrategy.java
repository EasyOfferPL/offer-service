package pl.easyoffer.offer_service.service.deduplication;

import pl.easyoffer.offer_service.model.dto.JobOfferRequestTO;
import pl.easyoffer.offer_service.model.entity.JobOfferEntity;

import java.util.Optional;

public interface DuplicateSearchStrategy {

    int priority();

    Optional<JobOfferEntity> find(JobOfferRequestTO request);

    default String getClassName() {
        return this.getClass().getSimpleName();
    }

}
