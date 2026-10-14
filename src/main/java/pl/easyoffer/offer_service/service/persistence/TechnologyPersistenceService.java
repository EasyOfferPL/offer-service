package pl.easyoffer.offer_service.service.persistence;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.easyoffer.offer_service.model.entity.TechnologyEntity;
import pl.easyoffer.offer_service.repository.TechnologyRepository;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TechnologyPersistenceService {

    private final TechnologyRepository technologyRepository;

    public Optional<TechnologyEntity> findByNameAndLevel(String name, Integer level) {
        return technologyRepository.findByNameAndLevel(name, level);
    }

    public TechnologyEntity save(TechnologyEntity technologyEntity) {
        return technologyRepository.save(technologyEntity);
    }

    public long countValues(String normalizationVersion) {
        return technologyRepository.countByNormalizationVersion(normalizationVersion);
    }

    public long countChangedValues(String normalizationVersion) {
        return technologyRepository.countChangedValues(normalizationVersion);
    }

    public long countOfferAssociations(String normalizationVersion) {
        return technologyRepository.countOfferAssociations(normalizationVersion);
    }

    public long countChangedOfferAssociations(String normalizationVersion) {
        return technologyRepository.countChangedOfferAssociations(normalizationVersion);
    }
}
