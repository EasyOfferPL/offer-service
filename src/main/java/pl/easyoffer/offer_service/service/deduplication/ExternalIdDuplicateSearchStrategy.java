package pl.easyoffer.offer_service.service.deduplication;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import pl.easyoffer.offer_service.model.dto.JobOfferRequestTO;
import pl.easyoffer.offer_service.model.entity.JobOfferEntity;
import pl.easyoffer.offer_service.service.persistence.JobOfferPersistenceService;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ExternalIdDuplicateSearchStrategy implements DuplicateSearchStrategy {

    private final JobOfferPersistenceService jobOfferPersistenceService;

    @Override
    public int priority() {
        return 10;
    }

    @Override
    public Optional<JobOfferEntity> find(JobOfferRequestTO request) {
        if (!StringUtils.hasText(request.getExternalId())) {
            return Optional.empty();
        }
        return jobOfferPersistenceService.findByExternalId(request.getExternalId());
    }

}
