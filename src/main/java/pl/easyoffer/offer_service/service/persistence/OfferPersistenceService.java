package pl.easyoffer.offer_service.service.persistence;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import pl.easyoffer.offer_service.model.entity.OfferEntity;
import pl.easyoffer.offer_service.repository.OfferRepository;
import pl.easyoffer.offer_service.repository.projection.CategoryStatisticProjection;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OfferPersistenceService {

    private final OfferRepository offerRepository;

    public Page<OfferEntity> findAll(Pageable pageable) {
        return offerRepository.findAll(pageable);
    }

    public List<OfferEntity> findAll(Specification<OfferEntity> specification) {
        return offerRepository.findAll(specification);
    }

    public Optional<OfferEntity> findById(Long id) {
        return offerRepository.findById(id);
    }

    public Page<OfferEntity> search(Specification<OfferEntity> specification, Pageable pageable) {
        return offerRepository.findAll(specification, pageable);
    }

    public OfferEntity save(OfferEntity offer) {
        return offerRepository.save(offer);
    }

    public Optional<OfferEntity> findByExternalId(String externalId) {
        return offerRepository.findByExternalId(externalId);
    }

    public Optional<OfferEntity> findByCoreFields(String title, String companyName, String location) {
        return offerRepository.findByCoreFields(title, companyName, location);
    }

    public List<CategoryStatisticProjection> getCategoryStatistics(Pageable pageable) {
        return offerRepository.getCategoryStatistics(pageable);
    }

}
