package pl.easyoffer.offer_service.service;

import com.easyoffer.offer_client.to.OfferSearchRequest;
import com.easyoffer.offer_client.to.OfferTO;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.easyoffer.offer_service.mapper.OfferMapper;
import pl.easyoffer.offer_service.service.persistence.OfferPersistenceService;
import pl.easyoffer.offer_service.util.OfferSpecificationBuilder;

import java.util.List;

import static java.lang.String.format;

@Service
@RequiredArgsConstructor
public class OfferService {

    private final OfferPersistenceService offerPersistenceService;

    public List<OfferTO> searchOffers(OfferSearchRequest searchRequest) {
        var specification = OfferSpecificationBuilder.builder()
                .withIds(searchRequest.getIds())
                .build();
        return OfferMapper.INSTANCE.map(offerPersistenceService.search(specification));
    }

    public List<OfferTO> getAll() {
        return OfferMapper.INSTANCE.map(offerPersistenceService.getAll());
    }

    public OfferTO getById(Long id) {
        var offerEntity = offerPersistenceService.getById(id)
                .orElseThrow(() -> new EntityNotFoundException(format("Entity not found {offerId=%s}", id)));
        return OfferMapper.INSTANCE.map(offerEntity);
    }

    @Transactional
    public OfferTO save(OfferTO offerTO) {
        var mappedEntity = OfferMapper.INSTANCE.map(offerTO);
        return OfferMapper.INSTANCE.map(offerPersistenceService.save(mappedEntity));
    }

    @Transactional
    public OfferTO update(OfferTO offerTO) {
        var offerId = offerTO.getId();
        var storedEntity = offerPersistenceService.getById(offerId)
                .orElseThrow(() -> new EntityNotFoundException(format("Entity not found {offerId=%s}", offerId)));

        OfferMapper.INSTANCE.update(offerTO, storedEntity);
        var updatedEntity = offerPersistenceService.update(storedEntity);

        return OfferMapper.INSTANCE.map(updatedEntity);
    }

    @Transactional
    public void deleteById(Long id) {
        offerPersistenceService.delete(id);
    }

}
