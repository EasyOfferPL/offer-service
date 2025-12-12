package pl.easyoffer.offer_service.service.persistence;

import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import pl.easyoffer.offer_service.model.OfferEntity;
import pl.easyoffer.offer_service.repository.OfferRepository;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OfferPersistenceService {

    //todo events for modify methods

    private final OfferRepository offerRepository;

    public List<OfferEntity> search(Specification<OfferEntity> specification) {
        return offerRepository.findAll(specification);
    }

    public List<OfferEntity> getAll() {
        return offerRepository.findAll();
    }

    public Optional<OfferEntity> getById(Long id) {
        return offerRepository.findById(id);
    }

    public OfferEntity save(OfferEntity offerEntity) {
        return offerRepository.saveAndFlush(offerEntity);
    }

    public OfferEntity update(OfferEntity offerEntity) {
        return offerRepository.saveAndFlush(offerEntity);
    }

    public void delete(Long id) {
        offerRepository.deleteById(id);
    }

}
