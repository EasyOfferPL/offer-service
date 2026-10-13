package pl.easyoffer.offer_service.service.deduplication;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import pl.easyoffer.offer_service.model.entity.OfferEntity;
import pl.easyoffer.offer_service.model.to.OfferRequestTO;
import pl.easyoffer.offer_service.service.persistence.OfferPersistenceService;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class MissingExternalIdDuplicateSearchStrategy implements DuplicateSearchStrategy {

    private static final int PRIORITY = 20;

    private final OfferPersistenceService offerPersistenceService;

    @Override
    public int priority() {
        return PRIORITY;
    }

    @Override
    public Optional<OfferEntity> find(OfferRequestTO request) {
        if (StringUtils.hasText(request.getExternalId())) {
            return Optional.empty();
        }
        return CrossSourceFingerprintV1.calculate(
                        request.getTitle(), request.getCompanyName(), request.getLocation()
                )
                .flatMap(fingerprint -> offerPersistenceService
                        .findBySourceAndMissingExternalIdAndDeduplicationFingerprint(
                                request.getSource(), fingerprint
                        ));
    }
}
