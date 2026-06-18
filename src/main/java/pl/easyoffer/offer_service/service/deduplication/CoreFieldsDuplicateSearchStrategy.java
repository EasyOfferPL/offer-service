package pl.easyoffer.offer_service.service.deduplication;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pl.easyoffer.offer_service.model.entity.OfferEntity;
import pl.easyoffer.offer_service.model.to.OfferRequestTO;
import pl.easyoffer.offer_service.service.persistence.OfferPersistenceService;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class CoreFieldsDuplicateSearchStrategy implements DuplicateSearchStrategy {

    private final OfferPersistenceService offerPersistenceService;

    @Override
    public int priority() {
        return 20;
    }

    @Override
    public Optional<OfferEntity> find(OfferRequestTO request) {
        return offerPersistenceService.findByCoreFields(
                request.getTitle(),
                request.getCompanyName(),
                request.getLocation()
        );
    }

}
