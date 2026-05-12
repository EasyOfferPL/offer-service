package pl.easyoffer.offer_service.service.deduplication;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pl.easyoffer.offer_service.model.dto.JobOfferRequestTO;
import pl.easyoffer.offer_service.model.entity.JobOfferEntity;
import pl.easyoffer.offer_service.service.persistence.JobOfferPersistenceService;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class CoreFieldsDuplicateSearchStrategy implements DuplicateSearchStrategy {

    private final JobOfferPersistenceService jobOfferPersistenceService;

    @Override
    public int priority() {
        return 20;
    }

    @Override
    public Optional<JobOfferEntity> find(JobOfferRequestTO request) {
        return jobOfferPersistenceService.findByCoreFields(
                request.getTitle(),
                request.getCompanyName(),
                request.getLocation()
        );
    }

}
